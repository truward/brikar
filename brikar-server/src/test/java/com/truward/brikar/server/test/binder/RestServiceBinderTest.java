package com.truward.brikar.server.test.binder;

import com.truward.brikar.server.test.fixture.ProfileModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Alexander Shabanov
 */
public class RestServiceBinderTest {


  public interface ProfileRestService {

    @RequestMapping("/profile/{id}")
    @ResponseBody
    ProfileModel.Profile getProfile(@PathVariable("id") int id);

    @RequestMapping(value = "/profile", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void saveProfile(@RequestBody ProfileModel.Profile profile);
  }
}
