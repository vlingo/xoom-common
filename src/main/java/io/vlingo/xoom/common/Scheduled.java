// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

/**
 * Protocol implemented for notifications from a {@code Scheduler}.
 * @param <T> the type of data associated with each notification
 */
public interface Scheduled<T> {
  /**
   * Sent when a one-time or repeating interval is reached.
   * @param scheduled the Scheduled instance receiving the notification
   * @param data the T data associated with the Scheduled notification(s)
   */
  void intervalSignal(final Scheduled<T> scheduled, final T data);
}
