package io.vlingo.xoom.common.implnative;

import io.vlingo.xoom.common.identity.IdentityGenerator;
import io.vlingo.xoom.common.message.Message;
import io.vlingo.xoom.common.message.MessageExchangeReader;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import java.util.Date;

public final class NativeImpl {
    @CEntryPoint(name = "Java_io_vlingo_xoom_commonnative_Native_from")
    public static int from(@CEntryPoint.IsolateThreadContext long isolateId, CCharPointer message) {
        final String messageString = CTypeConversion.toJavaString(message);
        MessageExchangeReader.from(new Message() {

            @Override
            public String id() {
                // TODO Auto-generated method stub
                return new IdentityGenerator.NameBasedIdentityGenerator().generate().toString();
            }

            @Override
            public Date occurredOn() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String payload() {
                // TODO Auto-generated method stub
                return messageString;
            }

            @Override
            public String type() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String version() {
                // TODO Auto-generated method stub
                return null;
            }

        });
        return 0;
    }
}
