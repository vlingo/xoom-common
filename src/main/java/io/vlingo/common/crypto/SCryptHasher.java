package io.vlingo.common.crypto;

import com.lambdaworks.crypto.SCryptUtil;

public class SCryptHasher implements Hasher {
  private final int N_costFactor;
  private final int r_blocksize;
  private final int p_parallelization;
  
  public SCryptHasher(final int N_costFactor, final int r_blocksize, final int p_parallelization) {
    this.N_costFactor = N_costFactor;
    this.r_blocksize = r_blocksize;
    this.p_parallelization = p_parallelization;
  }
  
  @Override
  public String hash(final String plainSecret) {
    final String hashed = SCryptUtil.scrypt(plainSecret, N_costFactor, r_blocksize, p_parallelization);
    return hashed;
  }

  @Override
  public boolean verify(final String plainSecret, final String hashedSecret) {
    return SCryptUtil.check(plainSecret, hashedSecret);
  }
}
