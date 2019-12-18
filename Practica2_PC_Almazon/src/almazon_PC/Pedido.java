package almazon_PC;

import java.util.List;

public class Pedido {

	int num_pedido;
	List<Integer> lista_productos_cliente;
	long nombre_Cliente;
	Thread hiloCliente;
	
	public Pedido(int num_pedido, List<Integer> lista_productos_cliente, long cliente, Thread hiloCliente) {
		this.num_pedido = num_pedido;
		this.lista_productos_cliente = lista_productos_cliente;
		this.nombre_Cliente = cliente;
		this.hiloCliente = hiloCliente;
	}
	

	public int getNum_pedido() {
		return num_pedido;
	}

	public List<Integer> getLista_productos_cliente() {
		return lista_productos_cliente;
	}

	public long getNombre_Cliente() {
		return nombre_Cliente;
	}


	public Pedido() {

	}
	
	
	
}
