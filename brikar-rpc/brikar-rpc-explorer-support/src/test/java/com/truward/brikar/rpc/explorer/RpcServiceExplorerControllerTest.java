package com.truward.brikar.rpc.explorer;

import com.truward.brikar.rpc.ServletRpcBinding;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Alexander Shabanov
 */
public class RpcServiceExplorerControllerTest {
  private RpcServiceExplorerController explorerController;

  @Before
  public void init() {
    explorerController = new RpcServiceExplorerController();
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowIllegalArgumentIfRpcBindingIsNull() {
    explorerController.setRpcBinding(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowIllegalArgumentIfRpcBindingsAreNull() {
    explorerController.setRpcBindings(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentIfSomeRpcBindingsAreNull() {
    explorerController.setRpcBindings(Collections.<ServletRpcBinding>singletonList(null));
  }
}
