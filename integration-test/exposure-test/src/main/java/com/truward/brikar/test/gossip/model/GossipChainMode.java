package com.truward.brikar.test.gossip.model;

/**
 * @author Alexander Shabanov
 */
public enum GossipChainMode {
  /** Tells this gossiper to call next element in the gossip chain. */
  NEXT,

  /** Tells this gossiper to be the end of the gossip chain. */
  END
}
