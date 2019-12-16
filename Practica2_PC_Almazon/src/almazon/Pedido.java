package almazon;

import java.util.List;

public class Pedido {

	int num_pedido;
	List<Integer> lista_productos_cliente;

	public Pedido(int num_pedido, List<Integer> lista_productos_cliente) {
		this.num_pedido = num_pedido;
		this.lista_productos_cliente = lista_productos_cliente;
	}
	
	
	
}
