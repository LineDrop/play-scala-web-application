package utilities

import java.math.BigInteger
import java.security.SecureRandom

object Random {

  def alphanumeric : String = {
    // 128 bits with 32 radix (base)
    new BigInteger(128, new SecureRandom).toString(32)
  }
}
