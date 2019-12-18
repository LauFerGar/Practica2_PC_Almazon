package almazon_PC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Almazon {

	private ReentrantLock lock = new ReentrantLock();
	private ReentrantLock inc_pedido = new ReentrantLock();
	private Condition adminLock = lock.newCondition();
	private Condition pago_realizado = lock.newCondition();

	private Semaphore ver_almacen = new Semaphore(1);
	private Semaphore reponer_almacen = new Semaphore(1);

	private Exchanger<Pedido> exPedido = new Exchanger<Pedido>();
	private Exchanger<Integer> producto_reponer = new Exchanger<Integer>();

	public volatile int num_pedido = 0;
	public volatile int[] almacen = {10,10,10,10,20,20,20,20,20,20};
	//public volatile int[] almacen = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	public volatile boolean reponer = false; 
	 
	List<Pedido> lista_pedidos = new ArrayList<Pedido>();

	public void Cliente() throws InterruptedException {
			while (true) {
				int pedido_cliente;
				List<Integer> lista_productos_cliente = new ArrayList<Integer>();
				long nombre_cliente = Thread.currentThread().getId();

				inc_pedido.lock();
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
					inc_pedido.unlock();
				}
				Pedido p = new Pedido(pedido_cliente, lista_productos_cliente, nombre_cliente, Thread.currentThread());
				lista_pedidos.add(p);
				exPedido.exchange(p);

				lock.lock();
				try {
					// Mientras no se ha tramitado el pedido el cliente espera
					while (lock.hasWaiters(adminLock))
						adminLock.await();

				} catch (InterruptedException e) {
					System.out.println("Se ha producido un fallo en el pedido y el Cliente tiene que pedir de nuevo");
					break;
				} finally {
					lock.unlock();
				}

				System.out.println(
						"El cliente " + nombre_cliente + " esta esperando para pagar el pedido " + p.num_pedido);

				lock.lock();
				try {
					while (lock.hasWaiters(pago_realizado))
						pago_realizado.await();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					System.out.println("Se ha producido un fallo en el pedido y el Cliente tiene que pedir de nuevo");
				} finally {
					lock.unlock();
					Thread.sleep(5000);
				}

				System.out.println("El cliente " + nombre_cliente + " ha pagado el pedido " + p.num_pedido);
		}

	}

	public void EmpleadoAdministrativo() throws InterruptedException {
		while (true) {
				Pedido pAdmin = exPedido.exchange(null);
				while (pAdmin == null)
					pAdmin = exPedido.exchange(null);

				System.out.println("Admin recibe pedido del cliente: " + pAdmin.nombre_Cliente
						+ " con numero de pedido: " + pAdmin.num_pedido);

				lock.lock();
				try {
					for (int producto : pAdmin.lista_productos_cliente) {
						System.out.println("El cliente " + pAdmin.nombre_Cliente + " pide el producto " + producto);
					}
				} finally {
					lock.unlock();
				}

				lock.lock();
				try {
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
					break;
				} finally {
					lock.unlock();
				}

				System.out.println("Esperando pago del pedido " + pAdmin.num_pedido);

				lock.lock();
				try {
					pago_realizado.signal();
					Thread.sleep(500);
				} catch (InterruptedException e) {
				} finally {
					lock.unlock();
				}

				System.out.println("Admin - Pago realizado del cliente: " + pAdmin.nombre_Cliente);

				System.out.println("Admin - Email enviado al cliente: " + pAdmin.nombre_Cliente);
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
				Thread.sleep(1000);
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
