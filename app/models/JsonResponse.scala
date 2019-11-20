package models

object JsonResponse {

  def get(status: JsonStatus.Value, message: String) : String = {
    // Format status and message into JSON
    s"""{"status":"success","message":"$message"}"""
  }

}
