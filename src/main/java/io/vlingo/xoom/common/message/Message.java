// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.message;

import java.util.Date;

import io.vlingo.xoom.common.version.SemanticVersion;

public interface Message {
  String id();
  Date occurredOn();
  <T> T payload();
  String type();
  String version();
  default SemanticVersion semanticVersion() {
    return SemanticVersion.from(version());
  }
}
