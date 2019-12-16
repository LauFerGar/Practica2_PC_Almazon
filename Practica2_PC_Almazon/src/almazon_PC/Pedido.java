package almazon_PC;

import java.util.List;

public class Pedido {

	int num_pedido;
	List<Integer> lista_productos_cliente;
	String nombre_Cliente;
	
	public Pedido(int num_pedido, List<Integer> lista_productos_cliente, String nombre_Cliente) {
		this.num_pedido = num_pedido;
		this.lista_productos_cliente = lista_productos_cliente;
		this.nombre_Cliente = nombre_Cliente;
	}

	public int getNum_pedido() {
		return num_pedido;
	}

	public List<Integer> getLista_productos_cliente() {
		return lista_productos_cliente;
	}

	public String getNombre_Cliente() {
		return nombre_Cliente;
	}
	
	
	
}
