import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class TxHandler {

    private UTXOPool pool;

	/**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	double inputSum = 0;
    	double outputSum = 0;
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        HashSet<UTXO> usedOutputs = new HashSet<>();

        int index = 0;
    	for (Transaction.Input ip: tx.getInputs()) {
    		UTXO searchKey = new UTXO(ip.prevTxHash, ip.outputIndex);
			Transaction.Output txOp = pool.getTxOutput(searchKey);
			if (Objects.isNull(txOp)) { // condition 1
				return false;
			}
			if (!Crypto.verifySignature(txOp.address, tx.getRawDataToSign(index), ip.signature)) {
				return false; // condition 2
			}
			if (!usedOutputs.add(searchKey)) {
				return false; // condition 3
			}
			inputSum += txOp.value;
			index++;
    	}
		for (Transaction.Output op: outputs) {
        	if (op.value < 0) {
        		return false; // condition 4
        	}
        	outputSum += op.value;
        }
		if (outputSum > inputSum) {
			return false; // condition 5
		}
		return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	ArrayList<Transaction> newTransactions = new ArrayList<>();
        for (Transaction tx: possibleTxs) {
        	if (isValidTx(tx)) {
        		newTransactions.add(tx);
            	for (Transaction.Input ip: tx.getInputs()) {
            		UTXO searchKey = new UTXO(ip.prevTxHash, ip.outputIndex);
            		pool.removeUTXO(searchKey);
            	}
            	int index = 0;
            	for (Transaction.Output op: tx.getOutputs()) {
            		UTXO newOp = new UTXO(tx.getHash(), index);
            		pool.addUTXO(newOp, op);
            		++index;
            	}
        	}
        }
        return newTransactions.toArray(new Transaction[newTransactions.size()]);
    }
}
