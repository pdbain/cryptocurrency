import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashMap;

import junit.framework.TestCase;

public class TestBlockChain extends TestCase {

	private static final int MAIN_HEIGHT = 6;
	private static final int SIDE_HEIGHT = 10;
	private PublicKey mainKey;
	private Block genesisBlock;
	private BlockChain chain;

	public void testMakeBlock() throws NoSuchAlgorithmException {
		Block mainBlock = makeBlock(genesisBlock, mainKey);
		Block sideBlock = makeBlock(genesisBlock, makePublicKey());
		LinkedHashMap<ByteArrayWrapper, Block> chain = new LinkedHashMap<>();
		chain.put(new ByteArrayWrapper(mainBlock.getHash()), mainBlock);
		chain.put(new ByteArrayWrapper(sideBlock.getHash()), sideBlock);
		assertEquals(2, chain.size());
	}

	
	public void testAddBlocks() throws NoSuchAlgorithmException {
		// add a linear chain of 8 blocks
		Block mainChain[] = new Block[MAIN_HEIGHT];
		mainChain[0] = genesisBlock;
		assertEquals(1, chain.getBlockHeight(genesisBlock));
		assertEquals(genesisBlock, chain.getMaxHeightBlock());
		
		for (int i = 1; i < MAIN_HEIGHT; ++i) {
			mainChain[i] = makeBlock(mainChain[i - 1], mainKey);
		}
		for (int i = 1; i < MAIN_HEIGHT; ++i) {
			assertEquals(i + 1, chain.getBlockHeight(mainChain[i]));
		}
		assertEquals(mainChain[MAIN_HEIGHT - 1], chain.getMaxHeightBlock());
		// create a branch off the oldest block
		Block sideChain[] = new Block[SIDE_HEIGHT];
		sideChain[0] = genesisBlock;
		PublicKey sideKey = makePublicKey();
		for (int i = 1; i < sideChain.length; ++i) {
			Block newBlock = makeBlock(sideChain[i - 1], sideKey);
			sideChain[i] = newBlock;
			assertEquals(i + 1, chain.getBlockHeight(newBlock));
			if (i < MAIN_HEIGHT) {
				Block maxHeightBlock = chain.getMaxHeightBlock();
				assertEquals(mainChain[MAIN_HEIGHT - 1], maxHeightBlock);
			} else {				
				assertEquals(newBlock, chain.getMaxHeightBlock());
			}
		}
		assertEquals(sideChain[sideChain.length - 1], chain.getMaxHeightBlock());
	}

	public void testMidpointBranch() throws NoSuchAlgorithmException {
		// add a linear chain of 8 blocks
		Block mainChain[] = new Block[MAIN_HEIGHT];
		mainChain[0] = genesisBlock;
		assertEquals(1, chain.getBlockHeight(genesisBlock));
		assertEquals(genesisBlock, chain.getMaxHeightBlock());
		
		for (int i = 1; i < MAIN_HEIGHT; ++i) {
			mainChain[i] = makeBlock(mainChain[i - 1], mainKey);
		}
		for (int i = 1; i < MAIN_HEIGHT; ++i) {
			assertEquals(i + 1, chain.getBlockHeight(mainChain[i]));
		}
		assertEquals(mainChain[MAIN_HEIGHT - 1], chain.getMaxHeightBlock());
		// create a branch off the oldest block
		Block sideChain[] = new Block[SIDE_HEIGHT];
		// create a branch mid chain
		for (int i = 0; i < 3; ++i) {
			sideChain[i] = mainChain[i];
		}
		PublicKey sideKey = makePublicKey();
		for (int i = 3; i < sideChain.length; ++i) {
			Block newBlock = makeBlock(sideChain[i - 1], sideKey);
			sideChain[i] = newBlock;
			int h = chain.getBlockHeight(newBlock);
			assertEquals(i + 1, h);
			if (h <= MAIN_HEIGHT) {
				Block maxHeightBlock = chain.getMaxHeightBlock();
				assertEquals("side block " + i + " height " + h, mainChain[mainChain.length - 1], maxHeightBlock);
			} else {				
				assertEquals(newBlock, chain.getMaxHeightBlock());
			}
		}
		assertEquals(sideChain[sideChain.length - 1], chain.getMaxHeightBlock());
		
	}
	
	public void testAging() {
		Block mainChain[] = new Block[15];
		mainChain[0] = genesisBlock;
		for (int i = 1; i < mainChain.length; ++i) {
			Block newBlock = makeBlock(mainChain[i - 1], mainKey);
			mainChain[i] = newBlock;
			for (int j = 0; j <= i; ++j) {
				boolean valid = (i - j) < BlockChain.CUT_OFF_AGE;
				boolean present = chain.contains(mainChain[j]);
				assertEquals("newest="+i+" block "+j+" present:", valid, present);
			}
		}
	}

	private Block makeBlock(Block prevBlock, PublicKey theKey) {
		assertTrue(chain.contains(prevBlock));
		Block newBlock = new Block(prevBlock.getHash(), theKey);
		newBlock.addTransaction(newBlock.getCoinbase());
		newBlock.finalize();
		assertTrue(chain.addBlock(newBlock));
		return newBlock;
	}

	@Override
	public void setUp() throws NoSuchAlgorithmException {
		mainKey = makePublicKey();
		genesisBlock = new Block(null, mainKey);
		genesisBlock.finalize();
		chain = new BlockChain(genesisBlock);
	}

	private PublicKey makePublicKey() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair keyPair = kpg.generateKeyPair();
		PublicKey public1 = keyPair.getPublic();
		return public1;
	}
}
