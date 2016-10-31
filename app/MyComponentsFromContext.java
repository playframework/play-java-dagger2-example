import dagger.Component;
import dagger.Module;
import dagger.Provides;
import play.DefaultApplication;
import play.inject.Injector;
import play.routing.RoutingDsl;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.time.Clock;

import static play.mvc.Results.ok;

class MyComponentsFromContext extends play.api.BuiltInComponentsFromContext {

    @Inject
    Clock clock;

    @Inject
    public MyComponentsFromContext(play.ApplicationLoader.Context context) {
        super(context.underlying());
    }

    @Override
    public play.api.routing.Router router() {
            return new RoutingDsl()
                    .GET("/").routeTo(() -> ok("Hello, time is " + clock.millis()))
                    .build().asScala();
    }

    public play.Application javaApplication() {
        Injector injector = new play.inject.DelegateInjector(super.injector());
        return new DefaultApplication(super.application(), injector);
    }
}


@Singleton
@Component(modules = { ApplicationModule.class })
interface MyComponentsFactory {
   MyComponentsFromContext componentsFromContext();
}

@Module
class ApplicationModule {

    private final play.ApplicationLoader.Context context;

    public ApplicationModule(play.ApplicationLoader.Context context) {
        this.context = context;
    }

    @Provides
    play.ApplicationLoader.Context providesContext() {
        return this.context;
    }

    @Provides
    public Clock providesClock() {
        return java.time.Clock.systemUTC();
    }
}
