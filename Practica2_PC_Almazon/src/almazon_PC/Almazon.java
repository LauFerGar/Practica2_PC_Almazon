package almazon_PC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Almazon {

	//Cerrojo para Cliente y Admin
	private ReentrantLock lock = new ReentrantLock();
	
	private ReentrantLock inc = new ReentrantLock();

	//Condicion de cerrojo de Administrador para despertar al cliente para que pague
	private Condition adminLock = lock.newCondition();
	//Condicion de cerrojo de Cliente para avisar que ha mandado el pago
	private Condition pago_realizado = lock.newCondition();

	//Semaforo que controla el acceso al almacen de productos
	private Semaphore ver_almacen = new Semaphore(1);
	//Semaforo que controla la variable booleana que dice si hay que reponer o no
	private Semaphore reponer_almacen = new Semaphore(1);

	//Exchanger que pasa el pedido del Cliente al Admin
	private Exchanger<Pedido> exPedido = new Exchanger<Pedido>();
	//Exchanger que pasa el productoq ue hay que reponer
	private Exchanger<Integer> producto_reponer = new Exchanger<Integer>();

	//Numero de pedido
	public volatile int num_pedido = 0;
	//Almacen
	public volatile int[] almacen = {10,10,10,10,20,20,20,20,20,20};
	//Booleano que dice si hay que reponer algo o no
	public volatile boolean reponer = false; 
	
	//Lista de pedidos que ve Admin
	List<Pedido> lista_pedidos = new ArrayList<Pedido>();

	public void Cliente() throws InterruptedException {
			while (true) {
				int pedido_cliente;
				List<Integer> lista_productos_cliente = new ArrayList<Integer>();
				long nombre_cliente = Thread.currentThread().getId();

				Thread.sleep(2000);
				
				inc.lock();
				try {
					pedido_cliente = num_pedido + 1;

					int num_productos = (int) (Math.random() * 3 + 1);

					for (int i = 0; i < num_productos; i++) {
						int producto = (int) (Math.random() * almacen.length);
						lista_productos_cliente.add(producto);
					}

					System.out.println("El cliente " + nombre_cliente + " realiza pedido");
					num_pedido++;
				} finally {
					inc.unlock();
				}
				Pedido p = new Pedido(pedido_cliente, lista_productos_cliente, nombre_cliente, Thread.currentThread());
				lista_pedidos.add(p);
				
				exPedido.exchange(p);
				
				Thread.sleep(2000);
				
				lock.lock();
				try {
					
					// Mientras no se ha tramitado el pedido el cliente espera
					while (lock.hasWaiters(adminLock))
						adminLock.await();
					System.out.println(
							"El cliente " + nombre_cliente + " esta esperando para pagar el pedido " + p.num_pedido);

				} catch (InterruptedException e) {
					System.out.println("Se ha producido un fallo en el pedido y el Cliente tiene que pedir de nuevo");
				} finally {
					lock.unlock();
				}
				
				Thread.sleep(2000);

				lock.lock();
				try {
					while (lock.hasWaiters(pago_realizado))
						pago_realizado.await();
					
				} catch (InterruptedException e) {
					System.out.println("Se ha producido un fallo en el pedido y el Cliente tiene que pedir de nuevo");
				} finally {
					lock.unlock();
					Thread.sleep(2000);
					System.out.println("El cliente " + nombre_cliente + " ha pagado el pedido " + p.num_pedido);
				}
				
		}

	}

	public void EmpleadoAdministrativo() throws InterruptedException {
		while (true) {
				Pedido pAdmin = exPedido.exchange(null);
				while (pAdmin == null)
					pAdmin = exPedido.exchange(null);

				System.out.println("Admin recibe pedido del cliente: " + pAdmin.nombre_Cliente
						+ " con numero de pedido: " + pAdmin.num_pedido);
				
				Thread.sleep(2000);

				lock.lock();
				try {
					for (int producto : pAdmin.lista_productos_cliente) {
						System.out.println("El cliente " + pAdmin.nombre_Cliente + " pide el producto " + producto);
					}
					System.out.println("Revisando Datos...");
					for (int producto : pAdmin.lista_productos_cliente) {
						if (almacen[producto] <= 0) {
							System.out.println("El producto " + producto + " esta agotado");
							pAdmin.hiloCliente.interrupt();
							Thread.currentThread().interrupt();
						} 
						else if(almacen[producto]<=5) {
							reponer_almacen.acquire();
							reponer=true;
							producto_reponer.exchange(producto);
							reponer_almacen.release();
							
							Thread.sleep(2000);
							
							ver_almacen.acquire();
							almacen[producto]--;
							ver_almacen.release();
						}
						else {
							ver_almacen.acquire();
							almacen[producto]--;
							ver_almacen.release();
						}
						System.out.println("Quedan " + almacen[producto] + " articulos del producto " + producto);
					}
					Thread.sleep(500);
					System.out.println("Datos Correctos. Numero Pedido:  " + pAdmin.getNum_pedido()
							+ " - Nombre Cliente: " + pAdmin.getNombre_Cliente());
					System.out.println("Lista de Productos: ");

					for (int producto : pAdmin.lista_productos_cliente) {
						System.out.println("Producto verificado: " + producto);
					}

					adminLock.signal();// Despertamos al cliente
				} catch (InterruptedException e) {
					System.out.println(
							"Se cancela pedido del Cliente " + pAdmin.getNombre_Cliente() + " por falta de productos");
				}finally {
					lock.unlock();
				}
				
				Thread.sleep(2000);

				lock.lock();
				try {
					System.out.println("Esperando pago del pedido " + pAdmin.num_pedido);
					pago_realizado.signal();
					Thread.sleep(1000);
					System.out.println("Admin - Pago realizado del cliente: " + pAdmin.nombre_Cliente);

					System.out.println("Admin - Email enviado al cliente: " + pAdmin.nombre_Cliente);
				} catch (InterruptedException e) {
				} finally {
					lock.unlock();
					
				}
		}

	}

	public void EmpleadoRecogePedidos() throws InterruptedException {

	}

	public void EmpleadoEmpaquetaPedidos() throws InterruptedException {

	}

	public void EmpleadoRecogeLimpieza() throws InterruptedException {

	}

	public void EmpleadoEncargado() throws InterruptedException {

		while(true) {
		
			while(!reponer) {
				Thread.sleep(5000);
				System.out.println("Encargado - Estoy haciendo cosas");
			}
			
			int producto = producto_reponer.exchange(null);
			ver_almacen.acquire();
			almacen[producto]=20;
			System.out.println("El producto "+ producto + " se ha repuesto y ahora hay " + almacen[producto] + " articulos");
			ver_almacen.release();
			
		}
		
	}
}
