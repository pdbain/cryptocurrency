import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
	private Block root;
	private final TransactionPool txPool;

	LinkedHashMap<ByteArrayWrapper, Block> chain;
	
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        root = genesisBlock;
        txPool = new TransactionPool();
        chain = new LinkedHashMap<>();
        ByteArrayWrapper genesisHash = new ByteArrayWrapper(genesisBlock.getHash());
		chain.put(genesisHash, genesisBlock);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
    	HashMap<Block, Integer> blockHeights = new HashMap<>(chain.size());
    	int maxHeight = 0;
    	Block maxBlock = null;
    	blockHeights.put(root, Integer.valueOf(1));
    	for (Block b: chain.values()) {
    		int h = getBlockHeight(b, blockHeights);
    		if (h > maxHeight) {
    			maxHeight = h;
    			maxBlock = b;
    		}
    	}
    	return maxBlock;
    }

    private int getBlockHeight(Block cursor, HashMap<Block, Integer> blockHeights) {
    	Integer h = blockHeights.get(cursor);
    	if (nonNull(h)) {
    		return h.intValue();
    	} else {
    		byte[] prevBlockHash = cursor.getPrevBlockHash();
			Block parent = chain.get(new ByteArrayWrapper(prevBlockHash));
    		int myHeight = 1 + getBlockHeight(parent, blockHeights);
    		blockHeights.put(cursor, Integer.valueOf(myHeight));
    		return myHeight;
    	}
    }

     int getBlockHeight(Block cursor) {
     	HashMap<Block, Integer> blockHeights = new HashMap<>(chain.size());
     	blockHeights.put(root, Integer.valueOf(1));
    	return getBlockHeight(cursor, blockHeights);
    }
    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
    	return null; // TODO
    }
    
    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
    	ByteArrayWrapper blockHash = new ByteArrayWrapper(block.getHash());
    	chain.put(blockHash, block);
    	return false; // TODO

    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txPool.addTransaction(tx);
    }
    
    public void dumpChain() {
    	HashMap<Block, Integer> blockHeights = new HashMap<>(chain.size());
    	blockHeights.put(root, Integer.valueOf(1));
    	for (Block b: chain.values()) {
    		int h = getBlockHeight(b, blockHeights);
    		System.err.println("Block "+b.hashCode()+" height " + h);
    	}
    }
}