import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private Set<Transaction> initialTransactions;
    private Set<Transaction> proposedTransactions;
	private boolean[] myFollowees;
	int roundNum;
	private static boolean verbose = false;
	static int nodeCount = 0; // TODO DEBUG
	final int nodeNum;
	private final int numRounds;

	public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        roundNum = 0;
        nodeNum = nodeCount++;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        myFollowees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        initialTransactions = pendingTransactions;
        proposedTransactions = initialTransactions;
    }

	public Set<Transaction> sendToFollowers() {
        return proposedTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	Set<Transaction> knownTransactions = new HashSet<>();
    	Set<Transaction> popularTransactions = new HashSet<>();
    	++roundNum;
    	for (Candidate c : candidates) {
    		Transaction tx = c.tx;
    		if (knownTransactions.contains(tx)) {
    			popularTransactions.add(tx);
    		} else {
    			knownTransactions.add(tx);
    			if (initialTransactions.contains(tx)) {
    				popularTransactions.add(tx);
    			}
    		}
    	}
    	proposedTransactions = new HashSet<>(popularTransactions);
    	if (roundNum < numRounds/2) {
    		proposedTransactions.addAll(initialTransactions);
    		proposedTransactions.addAll(knownTransactions);
    	}
    }

	private static void log(String msg) {
		if (verbose ) {
			System.out.println(msg);
		}
		
	}

	private static void dumpTransactions(Set<Transaction> transSet) {
		int tlist[] = new int[transSet.size()];
		int i = 0;
		int hash = 0;
		for (Transaction t: transSet) {
			tlist[i] = t.id;
			hash += t.id;
			++i;
		}
		Arrays.sort(tlist);
		Arrays.stream(tlist).forEach(t -> log(Integer.toString(t)));
		log("hash="+hash);
	}
	
	public static int hashTransactions(Set<Transaction> transSet) {
		int hash = 0;
		for (Transaction t: transSet) {
			hash += t.id;
		}
		return hash;
	}
}
