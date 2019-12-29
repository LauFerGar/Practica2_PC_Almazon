package almazon_PC;

public class Personal {
	Almazon almazon; 
	
	static final int NUM_CLIENTES = 6; 
	static final int NUM_ADMIN = 2; 
	static final int NUM_ENCARGADO = 1;
	static final int NUM_RECOGE_PEDIDOS = 2;
	static final int NUM_EMPAQUETA_PEDIDOS = 2;
	static final int NUM_LIMPIEZA = 1;

	private void hiloCliente() {
		try {
			Thread.sleep(500);
			almazon.Cliente();
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloEncargado() {
		try {
			almazon.EmpleadoEncargado();
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloAdminM() {
		try {
			almazon.EmpleadoAdministrativo(0);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloRecogePedidosM() {
		try {
			almazon.EmpleadoRecogePedidos(0);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloEmpaquetaPedidosM() {
		try {
			almazon.EmpleadoEmpaquetaPedidos(0);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloLimpiarM() {
		try {
			almazon.EmpleadoLimpieza(0);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloAdminT() {
		try {
			almazon.EmpleadoAdministrativo(1);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloRecogePedidosT() {
		try {
			almazon.EmpleadoRecogePedidos(1);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloEmpaquetaPedidosT() {
		try {
			almazon.EmpleadoEmpaquetaPedidos(1);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void hiloLimpiarT() {
		try {
			almazon.EmpleadoLimpieza(1);
			
		} catch (InterruptedException e) {
			
		}
	}
	
	
	
	public void exec() {
		almazon = new Almazon(); 
		//Clientes
		for (int i = 0; i < NUM_CLIENTES; i++) {
			new Thread(() -> hiloCliente()).start();
		}
		
		//Encargado
		for (int i = 0; i < NUM_ENCARGADO; i++) {
			new Thread(() -> hiloEncargado()).start();
		}
		
		//Mañana
		for (int i = 0; i < NUM_ADMIN; i++) {
			new Thread(() -> hiloAdminM()).start();
		}
		
		for (int i = 0; i < NUM_RECOGE_PEDIDOS; i++) {
			new Thread(() -> hiloRecogePedidosM()).start();
		}
		
		for (int i = 0; i < NUM_EMPAQUETA_PEDIDOS; i++) {
			new Thread(() -> hiloEmpaquetaPedidosM()).start();
		}
		
		for (int i = 0; i < NUM_LIMPIEZA; i++) {
			new Thread(() -> hiloLimpiarM()).start();
		}
		
		//Tarde
		for (int i = 0; i < NUM_ADMIN; i++) {
			new Thread(() -> hiloAdminT()).start();
		}
		
		for (int i = 0; i < NUM_RECOGE_PEDIDOS; i++) {
			new Thread(() -> hiloRecogePedidosT()).start();
		}
		
		for (int i = 0; i < NUM_EMPAQUETA_PEDIDOS; i++) {
			new Thread(() -> hiloEmpaquetaPedidosT()).start();
		}
		for (int i = 0; i < NUM_LIMPIEZA; i++) {
			new Thread(() -> hiloLimpiarT()).start();
		}
		
		
		
	}
	
	public static void main(String[] args) {
		new Personal().exec();
	}
}
