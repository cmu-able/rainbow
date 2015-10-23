package incubator.il;

/**
 * Interface de um lock shared/exclusive. Os locks de shared/exclusive permitem
 * que v�rios threads o obtenham como shared mas apenas um o obtenha como
 * exclusive n�o podendo ser obtido como shared ou exclusive em simult�neo. O
 * lock existe num dos seguintes estados:
 * <ul>
 * <li><code>FREE</code>: O lock n�o est� adquirido por nenhum thread;</li>
 * <li><code>SHARED</code>: O lock est� adquirido por um ou mais threads em
 * modo partilhado. N�o existem threads � espera do modo exclusivo;</li>
 * <li><code>SHARED_BLOCKING</code>: O lock est� adqurido por um ou mais
 * threads me modo partilhado mas existem pedidos em fila de espera pendentes
 * por modo exclusivo. Novos pedidos em modo partilhado ou exclusivo s�o
 * colocados em fila de espera.</li>
 * <li><code>EXCLUSIVE</code>: o lock est� adqurido por um thread em modo
 * exclusivo.</li>
 * </ul> 
 */
public interface ISXLock {
	/**
	 * O lock exclusivo deve ser adquirido o mais rapidamente poss�vel. Ver o
	 * m�todo {@link #acquireExclusive(int, long)} para mais informa��es.
	 */
	int ASAP = 11;

	/**
	 * O lock exclusivo deve ser adquirido quando n�o estiver adquirido podendo
	 * esperar o tempo necess�rio. Ver o m�todo {@link #acquireExclusive(int,
	 * long)} para mais informa��es.
	 */
	int WHEN_AVAIlABLE = 12;

	/**
	 * Pode-se esperar at� um determinado per�odo de tempo para adquirir o
	 * lock exclusivo. Ver o m�todo {@link #acquireExclusive(int, long)} para
	 * mais informa��o.es
	 */
	int TIMED = 13;

	/**
	 * O lock est� livre.
	 */
	int FREE = 21;

	/**
	 * O lock est� adquirido em modo partilhado.
	 */
	int SHARED = 22;

	/**
	 * O lock est� adquirido em modo partilhado mas n�o s�o permitidos novos
	 * pedidos partilhados.
	 */
	int SHARED_BLOCKING = 23;

	/**
	 * O lock est� adquirido em modo exclusivo.
	 */
	int EXCLUSIVE = 24;

	/**
	 * Adquire o lock como shared.
	 */
	void acquireShared ();

	/**
	 * Tenta adquirir o lock de read sem esperar pela aquisi��o.
	 *
	 * @return conseguiu adquirir?
	 */
	boolean tryAcquireShared ();

	/**
	 * Adquire o lock em modo exclusivo. Dependendo do valor de
	 * <code>priority</code> e do estado actual do lock o resultado � o dado
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
	 * 		<td colspan="3">O lock � de imediato adquirido em modo
	 * exclusivo.</td>
	 * 		<td/>
	 * 		<td/>
	 * </tr>
	 * <tr>
	 * 		<th><code>SHARED</code></th>
	 * 		<td>O lock � colocado em modo <code>SHARED_BLOCKING</code> e o
	 * pedido de modo exclusivo � colocado na fila de espera.</td>
	 * 		<td rowspan="4">O thread fica a aguardar que o lock mude para
	 * estado <code>FREE</code>.</td>
	 * 		<td>O thread fica a aguardar durante o periodo de tempo
	 * especificado em <code>time</code> que o lock fique no estado
	 * <code>FREE</code>. Findo esse tempo comporta-se como se tivesse sido
	 * pedido como <code>ASAP</code>.</td>
	 * </tr>
	 * <tr>
	 * 		<th><code>SHARED_BLOCKING</code></th>
	 * 		<td rowspan="2">O pedido de modo exclusivo � colocado na fila de
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
	 * @param priority prioridade de aquisi��o: dever� ser um de
	 * {@link #ASAP}, {@link #WHEN_AVAIlABLE} ou {@link #TIMED}
	 * @param time tempo de espera (em milisegundos) caso a prioridade seja
	 * {@link #TIMED}. Ignorado nos restantes casos.
	 */
	void acquireExclusive (int priority, long time);
	
	/**
	 * Tenta adquirir o lock em modo exclusivo sem esperar.
	 * 
	 * @return foi adquirido o lock?
	 */
	boolean tryAcquireExclusive ();
	
	/**
	 * Este m�todo permite a um thread promover um lock partilhado para
	 * exclusivo. O m�todo � equivalente ao
	 * {@link #acquireExclusive(int, long)} com a diferen�a que este thread
	 * nunca perde o lock partilhado: quando restar apenas este thread com o
	 * lock partilhado, o lock � promovido a exclusivo. 
	 * 
	 * @param priority a prioridade de promo��o: dever� ser {@link #ASAP},
	 * {@link #WHEN_AVAIlABLE} ou {@link #TIMED}
	 * @param time tempo de espera (em milisegundos) caso a prioridade seja
	 * {@link #TIMED}. Ignorado nos restantes casos.
	 * 
	 * @throws IllegalSXOperationException o thread actual n�o det�m o lock
	 * partilhado
	 */
	void promoteToExclusive (int priority, long time);
	
	/**
	 * Tenta promover um lock partilhado para exclusivo n�o esperando por
	 * conseguir.
	 * 
	 * @return conseguiu-se promover o lock?
	 */
	boolean tryPromoteToExclusive ();
	
	/**
	 * Demove o lock exclusivo para partilhado.
	 * 
	 * @throws IllegalSXOperationException o thread actual n�o det�m o lock
	 * exclusivo
	 */
	void demoteToShared ();
	
	/**
	 * Obt�m o estado do lock.
	 * 
	 * @return {@link #FREE}, {@link #SHARED}, {@link #SHARED_BLOCKING} ou
	 * {@link #EXCLUSIVE}
	 */
	int getState ();
	
	/**
	 * Liberta o lock (partilhado ou exclusivo).
	 */
	void release ();
}
