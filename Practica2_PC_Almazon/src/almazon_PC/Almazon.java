package almazon_PC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Almazon {
	
//	private Object cerrojo = new Object(); 
	private ReentrantLock lock = new ReentrantLock(); 
	private Condition adminLock = lock.newCondition(); 
	
	
	private Exchanger<Pedido> exPedido = new Exchanger<Pedido>(); 
	
	static int num_pedido;
	static int[] almacen = {20,20,20,20,20,20,20,20,20,20,20};
	private Semaphore inc_pedido = new Semaphore(1);
	private Semaphore pago_pedido = new Semaphore(0);
	private Semaphore envio_email_cliente = new Semaphore(0);
	List<Pedido> lista_pedidos = new ArrayList<Pedido>();

	public void Cliente(int nombre_cliente) throws InterruptedException {
		
		inc_pedido.acquire();
		num_pedido++;
		inc_pedido.release();
		
		List<Integer> lista_productos_cliente = new ArrayList<Integer> ();
		int num_productos = (int)(Math.random()*3);
		
		for(int i=0; i<num_productos; i++) {
			int producto = (int)(Math.random()*almacen.length);
			lista_productos_cliente.add(producto);
		}
		
		Pedido p = new Pedido(num_pedido, lista_productos_cliente, nombre_cliente);
		exPedido.exchange(p); 
		
		System.out.println(Thread.currentThread().getName() +" Envio ejecutado por cliente");

//		synchronized(cerrojo) {
		lock.lock();
		try {
			lista_pedidos.add(p);
			System.out.println(Thread.currentThread().getName() +" añado el pedido del cliente " +nombre_cliente);
			adminLock.signal();//despertamos al admin porque acabo de añadir pedido
			Thread.sleep(500);
		}catch (InterruptedException e) {}
		finally {
			lock.unlock();
		}
//		}
		
		System.out.println("->");
		lock.lock();
		try {
			//Mientras no se ha tramitado el pedido el cliente espera
			for (Pedido pedido : lista_pedidos) {
				if(pedido.getNum_pedido() != p.num_pedido)
					adminLock.await();
			}
			
		}catch (InterruptedException e) {}
		finally {
			lock.unlock();
		}

		System.out.println("->");
		
		System.out.println("Espero a pago del pedido "+p.nombre_Cliente);
		
		lock.lock();
			pago_pedido.acquire();
			Thread.sleep(500);
		lock.unlock();
		
		lock.lock();
			envio_email_cliente.release();
			Thread.sleep(500);
		lock.unlock();
			
		System.out.println("confirmado Pago cliente: " +p.nombre_Cliente);
		
	}
	
	public void EmpleadoAdministrativo() throws InterruptedException {
		
		Pedido pAdmin = exPedido.exchange(null);
		while(pAdmin==null)
			pAdmin = exPedido.exchange(null);
		
		System.out.println("Envio Recibido por Admin");
		
//		synchronized(cerrojo) {
		lock.lock();
		try {
			if(lista_pedidos.size()==0) {
				System.out.println("Estoy esperando");
					while(lista_pedidos.size()==0 )
						adminLock.await();
			}
//			p = lista_pedidos.get(0);
			for (int pedido : pAdmin.lista_productos_cliente) {
				System.out.println("El pedido: "+pedido); 
			}
			System.out.println("Del cliente: "+pAdmin.nombre_Cliente);
//			lista_pedidos.remove(0);
		}catch (InterruptedException e) {}
		finally {
			lock.unlock();
		}
//		}
		
//		synchronized (cerrojo) {
		lock.lock();
		try {
			System.out.println("Revisando Datos...");
			Thread.sleep(500);
			System.out.println("Datos Correctos");
			adminLock.signal();//despertamos al cliente
			System.out.println("Numero Pedido:  " + pAdmin.getNum_pedido());
			System.out.println("Nombre Cliente: " + pAdmin.getNombre_Cliente());
			System.out.println("Lista de Productos: ");
	//		p.lista_productos_cliente.listIterator();
			for (Pedido pedido : lista_pedidos) {
				System.out.println("Pedido numero: " +pedido.num_pedido);
			}
			
//			for (Iterator<Pedido> it = lista_pedidos.iterator(); it.hasNext();) {
//				Pedido pedido = (Pedido) it.next();
//				System.out.println("Pedido numero: "+pedido.num_pedido);
//			}
		}catch (InterruptedException e) {}
		finally {
			lock.unlock();
		}
		
		System.out.println("Esperando pago del pedido " +pAdmin.num_pedido);

		lock.lock();
			pago_pedido.release();
			Thread.sleep(500);
		lock.unlock();
		
		System.out.println("Pago realizado");
		
		lock.lock();
			envio_email_cliente.acquire();
			Thread.sleep(500);
		lock.unlock();
		
		System.out.println("Email enviado");
		
	}

}
