package controllers

import javax.inject._
import play.api.mvc._

class SitemapController  @Inject()(cc: ControllerComponents) extends AbstractController(cc){

  def sitemap = Action { implicit request: Request[AnyContent] =>
    Ok(s"""<?xml version="1.0" encoding="UTF-8"?>
    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <url>
        <loc>URL goes here</loc>
      </url>
    </urlset>
      """)
  }

}
