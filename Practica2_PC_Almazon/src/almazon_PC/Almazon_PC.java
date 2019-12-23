package almazon_PC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;

public class Almazon_PC {

	// Variable del numero de pedidos
	public volatile int num_pedido = 0;
	// Almacen
	public volatile int[] almacen = { 10, 10, 10, 10, 20, 20, 20, 20, 20, 20 };
	// Constante del numero de productos que puede llegar a pedir un cliente como
	// maximo
	static final int MAX_PRODUCT_CLIENT = 3;
	// Constantes del tamaño de las playas
	static final int MAX_PRODUCT_PLAYA = 20;

	// Mapa de la playa 1
	private Map<Integer, Integer> playa_1 = new HashMap<>();
	// Mapa de la playa 2
	private Map<Integer, Integer> playa_2 = new HashMap<>();

	// Semaforo para acceder al almacen
	private Semaphore sem_ver_almacen = new Semaphore(1);
	// Semaforo para lista de RecogePedidos
	private Semaphore sem_lista_recogePedidos = new Semaphore(1);

	// Semaforo para while
	private Semaphore exC = new Semaphore(1);
	// Semaforo para while
	private Semaphore exA = new Semaphore(1);

	// Semaforos para playas
	private Semaphore sem_playa_1 = new Semaphore(1);
	private Semaphore sem_playa_2 = new Semaphore(1);

	// Lista de los pedidos que se han realizado
	List<Pedido> lista_pedidos = new ArrayList<Pedido>();
	// Lista de los pedidos que van a procesar los Encargados RecogePedidos
	List<Pedido> lista_pedidos_recogePedidos = new ArrayList<Pedido>();
	// Lista de pedidos Especiales que estan mal
	List<Pedido> lista_pedidos_especial = new ArrayList<Pedido>();

	// Exchanger que pasa el pedido del Cliente al Admin
	private Exchanger<Pedido> exPedido = new Exchanger<Pedido>();

	// Exchanger que pasa indicar que se ha relizado el pedido
	private Exchanger<Thread> exRealizado = new Exchanger<Thread>();

	// Exchanger que pasa indicar que se ha relizado el pago
	private Exchanger<Thread> exPagado = new Exchanger<Thread>();

	public void Cliente() throws InterruptedException {

		while (true) {

			int nPedido = 0;
			List<Integer> lista = new ArrayList<Integer>();
			long nombre = Thread.currentThread().getId();

			// Aumentamos el contador del metodo del numero de pedido
			nPedido = num_pedido + 1;

			// Aumentamos contador de fuera del metodo
			num_pedido++;

			// Creamos un numero aleatorio del numero de productos que quiere el cliente
			int num_productos = (int) (Math.random() * MAX_PRODUCT_CLIENT + 1);

			// Generemos los productos que quiere el cliente y los aï¿½adimos a la lista del
			// producto.
			for (int i = 0; i < num_productos; i++) {
				int producto = (int) (Math.random() * almacen.length);
				lista.add(producto);
			}

			sem_ver_almacen.acquire();
			Pedido p = new Pedido(nPedido, lista, nombre, Thread.currentThread());

			lista_pedidos.add(p);
			sem_ver_almacen.release();

			System.out.println("Soy Cliente " + Thread.currentThread().getId() + " y he mandado el pedido " + nPedido);

			exC.acquire();
			exPedido.exchange(p);
//			System.out.println("cliente recoge exPedido: "+p.num_pedido);
			exC.release();

			exC.acquire();
			Thread cliente = exRealizado.exchange(null);
			while (cliente == null) {
				cliente = exRealizado.exchange(null);
			}
//			System.out.println("cliente recoge exRealizado: "+cliente.getId());
			exC.release();

//			System.out.println("pedido exPagado: "+p.hiloCliente.getId());
			exPagado.exchange(p.hiloCliente);

			Thread.sleep(10000);
		}

	}

	public void EmpleadoAdministrativo() throws InterruptedException {
		// if lista pedido_urgente y lista pedidos_para enviar estan vacias
		// si no/ pregunta cual esta llena y la gestiona if/else

		while (true) {
			exA.acquire();
			Pedido pAdmin = exPedido.exchange(null);
			while (pAdmin == null)
				pAdmin = exPedido.exchange(null);
//			System.out.println("Admin recoge exPedido pedido: "+pAdmin.num_pedido);
//			System.out.println("Admin recoge exPedido cliente: "+pAdmin.nombre_Cliente);
			exA.release();

			for (int producto : pAdmin.lista_productos_cliente) {
				System.out.println("El cliente " + pAdmin.nombre_Cliente + " pide el producto " + producto);
			}
			System.out.println("Revisando Datos...");
			for (int producto : pAdmin.lista_productos_cliente) {
				sem_ver_almacen.acquire();
				almacen[producto]--;
				sem_ver_almacen.release();
				System.out.println("Quedan " + almacen[producto] + " articulos del producto " + producto);
			}
			Thread.sleep(1000);
			System.out.println("Datos Correctos. Numero Pedido:  " + pAdmin.getNum_pedido() + " - Nombre Cliente: "
					+ pAdmin.getNombre_Cliente());

			System.out.println("Lista de Productos: ");
			for (int producto : pAdmin.lista_productos_cliente) {
				System.out.println("Cliente " + pAdmin.nombre_Cliente + "- producto verificado: " + producto);
			}

			// Si el pedido ha salido bien devolvemos el hilo del cliente. Si no devolvemos
			// un hilo aleatorio
			Thread.sleep(2000);

//			System.out.println("pedido mandado por exRealizado: "+pAdmin.hiloCliente.getId());
			exRealizado.exchange(pAdmin.hiloCliente);

			exA.acquire();
			Thread pagoCliente = exPagado.exchange(null);
			while (pagoCliente == null)
				pagoCliente = exPagado.exchange(null);

//			System.out.println("exPagado hilo: "+pagoCliente.getId());
//			System.out.println("cliente pAdmin: "+pAdmin.hiloCliente.getId());

			System.out.println("El cliente " + pagoCliente.getId() + " ha pagado el pedido " + pAdmin.num_pedido
					+ ". Procedemos a mandar Email");
			exA.release();

			sem_lista_recogePedidos.acquire();
			lista_pedidos_recogePedidos.add(pAdmin);
			sem_lista_recogePedidos.release();

		}

	}

	public void EmpleadoRecogePedidos() throws InterruptedException {
		
		while(true) {
			if(!lista_pedidos_especial.isEmpty()) {
				//Se realiza lo que tiene que hacer cuando hay pedidos mal hechos
			}
			else {
				if(!lista_pedidos_recogePedidos.isEmpty()) {
					//Sacamos de la lista el pedido que vamos a gestionar
					sem_lista_recogePedidos.acquire();
					Pedido pedidoRP = lista_pedidos_recogePedidos.get(0);
					lista_pedidos_recogePedidos.remove(0);
					sem_lista_recogePedidos.release();
					
					boolean realizado = false;
					
					while(!realizado) {
						if(playa_1.size()<=MAX_PRODUCT_PLAYA-pedidoRP.lista_productos_cliente.size()) {
							for (int producto : pedidoRP.lista_productos_cliente) {
								//Si hay hueco en la primera playa
								sem_playa_1.acquire();
								playa_1.put(pedidoRP.num_pedido, producto);
								System.out.println("EmpleadoRP "+ Thread.currentThread().getId() + " Se mete el producto " + producto + " del pedido " + pedidoRP.num_pedido + " en la playa 1");
								sem_playa_1.release();
							}
							realizado = true;
						}
						else if (playa_2.size()<=(MAX_PRODUCT_PLAYA-pedidoRP.lista_productos_cliente.size())){
							for (int producto : pedidoRP.lista_productos_cliente) {
								sem_playa_2.acquire();
								playa_2.put(pedidoRP.num_pedido, producto);
								System.out.println("EmpleadoRP "+ Thread.currentThread().getId() + " Se mete el producto " + producto + " del pedido " + pedidoRP.num_pedido + " en la playa 2");
								sem_playa_2.release();
							}
						realizado = true;
						}	
					}
				}
				else {
					System.out.println("Esperando productos en playa");
				}
			}
			Thread.sleep(1500);
		}
		
	}
}
