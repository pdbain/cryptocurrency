import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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
    	 int height = 1;
    	 while (cursor != root) {
     		byte[] prevBlockHash = cursor.getPrevBlockHash();
 			cursor = chain.get(new ByteArrayWrapper(prevBlockHash));
     		++height;
     	}
    	return height;
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
    	byte[] prevBlockHash = block.getPrevBlockHash();
    	if (isNull(prevBlockHash)) return false;
		ByteArrayWrapper prevHash = new ByteArrayWrapper(prevBlockHash);
		Block prevBlock = chain.get(prevHash);
		if (isNull(prevBlock)) return false;
		int blockHeight = getBlockHeight(prevBlock);
		if (blockHeight > CUT_OFF_AGE) return false;
    	if (CUT_OFF_AGE == blockHeight) {
    		trimChain(prevBlock); 		
    	}
    	chain.put(blockHash, block);
    	return true;

    }

    private void trimChain(Block start) {
		Block cursor = start;
		Block child = start;
		ByteArrayWrapper parentHash = null;
		HashMap<Block, Boolean> blockStatus = new HashMap<>(chain.size());
		do {
	    	byte[] parentHashBytes = cursor.getPrevBlockHash();
			parentHash = new ByteArrayWrapper(parentHashBytes);
			child = cursor;
			blockStatus.put(cursor, Boolean.FALSE);
			cursor = chain.get(parentHash);
		} while (cursor != root);
		blockStatus.put(root, Boolean.TRUE);
		removeOrphans(blockStatus);
		root = child;
	}

	private void removeOrphans(HashMap<Block, Boolean> blockStatus) {
    	for (Block b: blockStatus.keySet()) {
    		boolean status = isOrphan(b, blockStatus);
    	}
    	for (Entry<Block, Boolean> e: blockStatus.entrySet()) {
    		boolean isDead = e.getValue().booleanValue();
			if (isDead) {
    			chain.remove(new ByteArrayWrapper(e.getKey().getHash()));
    		}
    	}
	}

	private boolean isOrphan(Block cursor, HashMap<Block, Boolean> blockStatus) {
    	Boolean dead = blockStatus.get(cursor);
    	if (nonNull(dead)) {
    		return dead.booleanValue();
    	} else {
    		byte[] prevBlockHash = cursor.getPrevBlockHash();
			Block parent = chain.get(new ByteArrayWrapper(prevBlockHash));
    		boolean myStatus = isOrphan(parent, blockStatus);
    		blockStatus.put(cursor, Boolean.valueOf(myStatus));
    		return myStatus;
    	}
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
    
    boolean contains(Block b) {
    	return chain.containsKey(new ByteArrayWrapper(b.getHash()));
    }
}