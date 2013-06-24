package incubator.il;

/**
 * Interface de um lock shared/exclusive. Os locks de shared/exclusive permitem
 * que vários threads o obtenham como shared mas apenas um o obtenha como
 * exclusive não podendo ser obtido como shared ou exclusive em simultâneo. O
 * lock existe num dos seguintes estados:
 * <ul>
 * <li><code>FREE</code>: O lock não está adquirido por nenhum thread;</li>
 * <li><code>SHARED</code>: O lock está adquirido por um ou mais threads em
 * modo partilhado. Não existem threads à espera do modo exclusivo;</li>
 * <li><code>SHARED_BLOCKING</code>: O lock está adqurido por um ou mais
 * threads me modo partilhado mas existem pedidos em fila de espera pendentes
 * por modo exclusivo. Novos pedidos em modo partilhado ou exclusivo são
 * colocados em fila de espera.</li>
 * <li><code>EXCLUSIVE</code>: o lock está adqurido por um thread em modo
 * exclusivo.</li>
 * </ul> 
 */
public interface ISXLock {
	/**
	 * O lock exclusivo deve ser adquirido o mais rapidamente possível. Ver o
	 * método {@link #acquireExclusive(int, long)} para mais informações.
	 */
	public static final int ASAP = 11;
	
	/**
	 * O lock exclusivo deve ser adquirido quando não estiver adquirido podendo
	 * esperar o tempo necessário. Ver o método {@link #acquireExclusive(int,
	 * long)} para mais informações.
	 */
	public static final int WHEN_AVAIlABLE = 12;
	
	/**
	 * Pode-se esperar até um determinado período de tempo para adquirir o
	 * lock exclusivo. Ver o método {@link #acquireExclusive(int, long)} para
	 * mais informação.es
	 */
	public static final int TIMED = 13;
	
	/**
	 * O lock está livre.
	 */
	public static final int FREE = 21;
	
	/**
	 * O lock está adquirido em modo partilhado.
	 */
	public static final int SHARED = 22;
	
	/**
	 * O lock está adquirido em modo partilhado mas não são permitidos novos
	 * pedidos partilhados.
	 */
	public static final int SHARED_BLOCKING = 23;
	
	/**
	 * O lock está adquirido em modo exclusivo.
	 */
	public static final int EXCLUSIVE = 24;
	
	/**
	 * Adquire o lock como shared.
	 */
	public void acquireShared();
	
	/**
	 * Tenta adquirir o lock de read sem esperar pela aquisição.
	 * 
	 * @return conseguiu adquirir?
	 */
	public boolean tryAcquireShared();
	
	/**
	 * Adquire o lock em modo exclusivo. Dependendo do valor de
	 * <code>priority</code> e do estado actual do lock o resultado é o dado
	 * pela tabela seguinte:
	 * <br>
	 * <table border="1">
	 * 	<tr>
	 * 		<th></th>
	 * 		<th><code>ASAP</code></th>
	 * 		<th><code>WHEN_AVAILABLE</code></th>
	 * 		<th><code>TIMED</code></th>
	 * 	</tr>
	 * 	<tr>
	 * 		<th><code>FREE</code></th>
	 * 		<td colspan="3">O lock é de imediato adquirido em modo
	 * exclusivo.</td>
	 * 		<td/>
	 * 		<td/>
	 * </tr>
	 * <tr>
	 * 		<th><code>SHARED</code></th>
	 * 		<td>O lock é colocado em modo <code>SHARED_BLOCKING</code> e o
	 * pedido de modo exclusivo é colocado na fila de espera.</td>
	 * 		<td rowspan="4">O thread fica a aguardar que o lock mude para
	 * estado <code>FREE</code>.</td>
	 * 		<td>O thread fica a aguardar durante o periodo de tempo
	 * especificado em <code>time</code> que o lock fique no estado
	 * <code>FREE</code>. Findo esse tempo comporta-se como se tivesse sido
	 * pedido como <code>ASAP</code>.</td>
	 * </tr>
	 * <tr>
	 * 		<th><code>SHARED_BLOCKING</code></th>
	 * 		<td rowspan="2">O pedido de modo exclusivo é colocado na fila de
	 * espera.</td>
	 * 		<td/>
	 * 		<td/>
	 * </tr>
	 * <tr>
	 * 		<th><code>EXCLUSIVE</code></th>
	 * 		<td/>
	 * 		<td/>
	 * 		<td/>
	 * </tr>
	 * </table>
	 * <br>
	 * 
	 * @param priority prioridade de aquisição: deverá ser um de
	 * {@link #ASAP}, {@link #WHEN_AVAIlABLE} ou {@link #TIMED}
	 * @param time tempo de espera (em milisegundos) caso a prioridade seja
	 * {@link #TIMED}. Ignorado nos restantes casos.
	 */
	public void acquireExclusive(int priority, long time);
	
	/**
	 * Tenta adquirir o lock em modo exclusivo sem esperar.
	 * 
	 * @return foi adquirido o lock?
	 */
	public boolean tryAcquireExclusive();
	
	/**
	 * Este método permite a um thread promover um lock partilhado para
	 * exclusivo. O método é equivalente ao
	 * {@link #acquireExclusive(int, long)} com a diferença que este thread
	 * nunca perde o lock partilhado: quando restar apenas este thread com o
	 * lock partilhado, o lock é promovido a exclusivo. 
	 * 
	 * @param priority a prioridade de promoção: deverá ser {@link #ASAP},
	 * {@link #WHEN_AVAIlABLE} ou {@link #TIMED}
	 * @param time tempo de espera (em milisegundos) caso a prioridade seja
	 * {@link #TIMED}. Ignorado nos restantes casos.
	 * 
	 * @throws IllegalSXOperationException o thread actual não detém o lock
	 * partilhado
	 */
	public void promoteToExclusive(int priority, long time);
	
	/**
	 * Tenta promover um lock partilhado para exclusivo não esperando por
	 * conseguir.
	 * 
	 * @return conseguiu-se promover o lock?
	 */
	public boolean tryPromoteToExclusive();
	
	/**
	 * Demove o lock exclusivo para partilhado.
	 * 
	 * @throws IllegalSXOperationException o thread actual não detém o lock
	 * exclusivo
	 */
	public void demoteToShared();
	
	/**
	 * Obtém o estado do lock.
	 * 
	 * @return {@link #FREE}, {@link #SHARED}, {@link #SHARED_BLOCKING} ou
	 * {@link #EXCLUSIVE}
	 */
	public int getState();
	
	/**
	 * Liberta o lock (partilhado ou exclusivo).
	 */
	public void release();
}
