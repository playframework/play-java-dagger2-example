package controllers;

import java.time.Clock;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

    private Clock clock;

    @Inject
    public HomeController(Clock clock) {
        this.clock = clock;
    }

    public Result home() {
        return ok("Hello, time is " + clock.millis());
    }
}
