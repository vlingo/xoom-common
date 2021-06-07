package nativebuild;

import java.util.Date;

import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import io.vlingo.xoom.common.identity.IdentityGenerator;
import io.vlingo.xoom.common.message.Message;
import io.vlingo.xoom.common.message.MessageExchangeReader;

public final class NativeBuildEntryPoint {
  @CEntryPoint(name = "Java_io_vlingo_xoom_commonnative_Native_from")
  public static int from(@CEntryPoint.IsolateThreadContext long isolateId, CCharPointer message) {
    final String messageString = CTypeConversion.toJavaString(message);
    MessageExchangeReader.from(new Message() {

      @Override
      public String id() {
        return new IdentityGenerator.NameBasedIdentityGenerator().generate().toString();
      }

      @Override
      public Date occurredOn() {
        return null;
      }

      @Override
      @SuppressWarnings("unchecked")
      public String payload() {
        return messageString;
      }

      @Override
      public String type() {
        return null;
      }

      @Override
      public String version() {
        return null;
      }

    });
    return 0;
  }
}
