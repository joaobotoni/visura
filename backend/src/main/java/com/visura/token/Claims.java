package com.visura.token;

import java.util.UUID;

public record Claims(UUID subject, Integer session) {
}
