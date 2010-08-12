import wsgiref.handlers

from google.appengine.ext import webapp
from google.appengine.api import users
from google.appengine.ext import db
from google.appengine.api.urlfetch import fetch
from urllib2 import urlopen
from urllib2 import Request
from urllib2 import URLError
from urllib import quote
from urllib import unquote
from urllib import url2pathname
import re
import hashlib
import time
import os
from random import SystemRandom

class Item(db.Model):
  browser = db.StringProperty(required = True)
  url = db.StringProperty(required = True)
  date = db.DateTimeProperty(required = True, auto_now_add=True)

class Page(webapp.RequestHandler):
  def getBrowser(self):
    browser = self.request.get('browser')
    return re.sub('[^0-9a-zA-Z_]', '0', browser)

  def render(self, name):
    path = os.path.join(os.path.dirname(__file__), 'static/' + name)
    self.response.out.write(open(path).read().replace('$$', self.getBrowser()))

  def store(self):
    url = self.request.get('url')
    Item(browser = self.getBrowser(), url = url).put()

class MainPage(Page):
  def __init__(self):
    self.random = SystemRandom();

  def get(self):
    id = str(self.random.random())[2:]
    self.redirect("/me?browser=" + id)

class AndroidSubmit(Page):
  def post(self):
    self.store()
    self.response.headers['Content-type'] = 'text/plain'

class Send(Page):
  def get(self):
    self.render('send.html')

class Done(Page):
  def post(self):
    self.store()
    self.redirect("/send?browser=" + self.getBrowser())
    

class Poll(Page):
  def get(self):
    self.response.headers['Content-type'] = 'text/plain'
    items = db.GqlQuery(
        "SELECT * FROM Item WHERE browser = :1", self.getBrowser())
    for item in items:
      self.response.out.write(item.url);
      item.delete()
      break

class Me(Page):
  def get(self):
    self.render('sharetobrowser.html')

class Associate(Page):
  def get(self):
    self.render('associate.html')

application = webapp.WSGIApplication([
  ('/', MainPage),
  ('/me', Me),
  ('/poll', Poll),
  ('/send', Send),
  ('/done', Done),
  ('/submit', AndroidSubmit),
  ('/associate', Associate),
], debug=True)


def main():
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == '__main__':
  main()
