package almazon_PC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;

public class Almazon_PC {
	
	//Variable del numero de pedidos
	public volatile int num_pedido = 0;
	//Almacen
	public volatile int[] almacen = {10,10,10,10,20,20,20,20,20,20};
	//Constante del numero de productos que puede llegar a pedir un cliente como maximo
	static final int MAX_PRODUCT_CLIENT = 3;
	
	//Semaforo para acceder al almacen
	private Semaphore ver_almacen = new Semaphore(1);
	//Semaforo para lista de pedidos
	private Semaphore ver_lista_pagos = new Semaphore(1);
	//Semaforo para ver lista de pendientes
	private Semaphore ver_lista_pendientes = new Semaphore(1);
	
	//Lista de los pedidos que se han realizado
	List<Pedido> lista_pedidos = new ArrayList<Pedido>();
	
	//Lista de los pedidos pendientes
	List<Pedido> lista_pendientes = new ArrayList<Pedido>();
	
	//Lista de pagos
	List<Pedido> pago_pedido = new ArrayList<Pedido>();
	
	//Exchanger que pasa el pedido del Cliente al Admin
	private Exchanger<Pedido> exPedido = new Exchanger<Pedido>();
	
	
	public void Cliente() throws InterruptedException {
		
		while(true) {
			
			int nPedido = 0;
			List<Integer> lista = new ArrayList<Integer>();
			long nombre = Thread.currentThread().getId();
			
			//Aumentamos el contador del metodo del numero de pedido
			nPedido = num_pedido + 1;
			
			//Aumentamos contador de fuera del metodo
			num_pedido++;

			//Creamos un numero aleatorio del numero de productos que quiere el cliente
			int num_productos = (int) (Math.random() * MAX_PRODUCT_CLIENT + 1);

			//Generemos los productos que quiere el cliente y los añadimos a la lista del producto.
			for (int i = 0; i < num_productos; i++) {
				int producto = (int) (Math.random() * almacen.length);
				lista.add(producto);
			}
			
			Pedido p = new Pedido(nPedido, lista, nombre, Thread.currentThread());
			
			System.out.println("Soy Cliente " + Thread.currentThread().getId() + " y he mandado el pedido " + nPedido);
			
			ver_lista_pendientes.acquire();
			lista_pendientes.add(p);
			ver_lista_pendientes.release();
			
			while(!lista_pedidos.contains(p));
			
			Thread.sleep(2000);
			
			ver_lista_pagos.acquire();
			pago_pedido.add(p);
			ver_lista_pagos.release();
			
			Thread.sleep(10000);
		}
		
	}
	
	public void EmpleadoAdministrativo() throws InterruptedException {
		
		while(true) {
			
			boolean pagado = false;
			Pedido pAdmin = null;
			
			while(lista_pendientes.isEmpty());
			
			ver_lista_pendientes.acquire();
			pAdmin = lista_pendientes.get(0);
			lista_pendientes.remove(0);
			ver_lista_pendientes.release();
			
			for (int producto : pAdmin.lista_productos_cliente) {
				System.out.println("El cliente " + pAdmin.nombre_Cliente + " pide el producto " + producto);
			}
			System.out.println("Revisando Datos...");
			for (int producto : pAdmin.lista_productos_cliente) {
				ver_almacen.acquire();
				almacen[producto]--;
				ver_almacen.release();
				System.out.println("Quedan " + almacen[producto] + " articulos del producto " + producto);
			}
			Thread.sleep(1000);
			System.out.println("Datos Correctos. Numero Pedido:  " + pAdmin.getNum_pedido()
					+ " - Nombre Cliente: " + pAdmin.getNombre_Cliente());

			System.out.println("Lista de Productos: ");
			for (int producto : pAdmin.lista_productos_cliente) {
				System.out.println("Producto verificado: " + producto);
			}
			
			//Si el pedido ha salido bien devolvemos el hilo del cliente. Si no devolvemos un hilo aleatorio
			Thread.sleep(2000);
			lista_pedidos.add(pAdmin);
			
			Pedido pago;
			while(!pagado && pago_pedido.size()>0) {
				pago = pago_pedido.get(0);
				if(pago.num_pedido==pAdmin.num_pedido)
					pagado=true;
			}
			
			ver_lista_pagos.acquire();
			pago_pedido.remove(pAdmin);
			ver_lista_pagos.release();
			
			System.out.println("El cliente " + pAdmin.hiloCliente.getId() + " ha pagado el pedido " + pAdmin.num_pedido + ". Procedemos a mandar Email");
			
		}
		
	}
}
