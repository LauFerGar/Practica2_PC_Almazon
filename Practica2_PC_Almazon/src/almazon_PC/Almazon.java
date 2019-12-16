package almazon_PC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Almazon {
	
	static int num_pedido;
	static int[] almacen = {20,20,20,20,20,20,20,20,20,20,20};
	private Semaphore inc_pedido = new Semaphore(0);
	private Semaphore pago_pedido = new Semaphore(0);
	private Semaphore envio_email_cliente = new Semaphore(0);
	List<Pedido> lista_pedidos;

	private void Cliente(String nombre_cliente) throws InterruptedException {
		
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
		
		synchronized(lista_pedidos) {
			lista_pedidos.add(p);
			lista_pedidos.notify();
		}
		
		//Mientras no se ha tramitado el pedido el cliente espera
		while(lista_pedidos.contains(p));
		
		pago_pedido.release();
		
		envio_email_cliente.acquire();
		
	}
	
	private void EmpleadoAdministrativo() throws InterruptedException {
		
		Pedido p = null;
		
		synchronized(lista_pedidos) {
			if(lista_pedidos.size()==0)
				lista_pedidos.wait();
			p = lista_pedidos.get(0);
			lista_pedidos.remove(0);
		}
		
		System.out.println("Revisando Datos...");
		Thread.sleep(500);
		System.out.println("Datos Correctos");
		System.out.println("Numero Pedido:  " + p.getNum_pedido());
		System.out.println("Nombre Cliente: " + p.getNombre_Cliente());
		System.out.println("Lista de Productos: ");
		p.lista_productos_cliente.iterator();
		
		System.out.println("Esperando pago del pedido");
		pago_pedido.acquire();
		
		System.out.println("Pago realizado");
		
		envio_email_cliente.release();
		
		System.out.println("Email enviado");
		
	}

}
