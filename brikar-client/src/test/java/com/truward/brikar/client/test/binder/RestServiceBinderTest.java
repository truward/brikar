package com.truward.brikar.client.test.binder;

import com.truward.brikar.client.binder.RestServiceBinder;
import com.truward.brikar.client.binder.support.StandardRestServiceBinder;
import com.truward.brikar.client.test.fixture.ProfileModel;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Shabanov
 */
public class RestServiceBinderTest {

  @Test
  public void shouldBindRestService() throws URISyntaxException {
    // Given:
    final RestOperations restOperations = mock(RestOperations.class);

    final ProfileModel.Profile aGotProfile = ProfileModel.Profile.newBuilder().setId(1).setName("2").build();
    when(restOperations.exchange(new URI("http://127.0.0.1/profile/1"), HttpMethod.GET, new HttpEntity<>(null), ProfileModel.Profile.class))
        .thenReturn(new ResponseEntity<>(aGotProfile, HttpStatus.OK));

    final ProfileModel.Profile aFoundProfile = ProfileModel.Profile.newBuilder().setId(3).setName("4").build();
    when(restOperations.exchange(new URI("http://127.0.0.1/profile?query=test&limit=10"), HttpMethod.GET, new HttpEntity<>(null), ProfileModel.Profile.class))
        .thenReturn(new ResponseEntity<>(aFoundProfile, HttpStatus.OK));

    when(restOperations.exchange(new URI("http://127.0.0.1/profile"), HttpMethod.PUT, new HttpEntity<>(aGotProfile), Void.TYPE))
        .thenReturn(new ResponseEntity<Void>(HttpStatus.CREATED));

    final RestServiceBinder binder = new StandardRestServiceBinder(restOperations);
    final ProfileRestService service = binder.createClient("http://127.0.0.1", ProfileRestService.class);

    // When:
    final ProfileModel.Profile gotProfile = service.getProfile(1);
    final ProfileModel.Profile foundProfile = service.searchProfile("test", 10);
    service.saveProfile(gotProfile);

    // Then:
    assertEquals(aGotProfile, gotProfile);
    assertEquals(aFoundProfile, foundProfile);
  }


  public interface ProfileRestService {

    @RequestMapping("/profile/{id}")
    @ResponseBody
    ProfileModel.Profile getProfile(@PathVariable("id") int id);

    @RequestMapping("/profile")
    @ResponseBody
    ProfileModel.Profile searchProfile(@RequestParam("query") String value, @RequestParam("limit") int limit);

    @RequestMapping(value = "/profile", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void saveProfile(@RequestBody ProfileModel.Profile profile);
  }
}
