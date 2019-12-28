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


	public void setNum_pedido(int num_pedido) {
		this.num_pedido = num_pedido;
	}


	public void setLista_productos_cliente(List<Integer> lista_productos_cliente) {
		this.lista_productos_cliente = lista_productos_cliente;
	}


	public void setNombre_Cliente(long nombre_Cliente) {
		this.nombre_Cliente = nombre_Cliente;
	}


	public void setHiloCliente(Thread hiloCliente) {
		this.hiloCliente = hiloCliente;
	}
	
	
	
}
