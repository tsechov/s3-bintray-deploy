package hu.blackbelt.cd.bintray.deploy;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class MainJava implements RequestHandler<S3Event, Void>{



    @Override
    public Void handleRequest(S3Event s3Event, Context context) {
        new Main(context).deploy(s3Event);
        return null;
    }
}
