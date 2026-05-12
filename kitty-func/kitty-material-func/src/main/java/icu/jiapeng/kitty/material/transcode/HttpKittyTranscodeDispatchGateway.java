package icu.jiapeng.kitty.material.transcode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 需在应用中配置 {@code grpc.client.kitty-transcoder}。
 */
@Slf4j
@Component
public class HttpKittyTranscodeDispatchGateway implements TranscodeDispatchGateway {


    @Override
    public Optional<String> submit(String platformCode, TranscodeSubmitCommand command) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
