package com.truward.brikar.client.test.binder;

import com.truward.brikar.client.test.fixture.ProfileModel;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: move to brikar-client.
 * @author Alexander Shabanov
 */
public class RestServiceBinderTest {

  @Test
  public void shouldBindRestService() {
    // TODO: impl
  }


  public interface ProfileRestService {

    @RequestMapping("/profile/{id}")
    @ResponseBody
    ProfileModel.Profile getProfile(@PathVariable("id") int id);

    @RequestMapping("/profile")
    @ResponseBody
    ProfileModel.Profile searchProfile(@RequestParam("asd") String value);

    @RequestMapping(value = "/profile", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void saveProfile(@RequestBody ProfileModel.Profile profile);
  }
}
