// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

public class RepeatableCompletes<T> extends BasicCompletes<T> {

  public RepeatableCompletes(final Scheduler scheduler) {
    super(scheduler);
  }

  public RepeatableCompletes(final T outcome, final boolean succeeded) {
    super(outcome, succeeded);
  }

  public RepeatableCompletes(final T outcome) {
    super(outcome);
  }

  @Override
  public Completes<T> repeat() {
    sink.repeat();
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(O outcome) {
    super.with(outcome);
    sink.repeat();
    return (Completes<O>) this;
  }
}
