import wsgiref.handlers
import datetime

import webapp2

from google.appengine.api import users
from google.appengine.api import channel
from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext.webapp import template
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
import logging

class Item(db.Model):
  browser = db.StringProperty(required = True)
  url = db.StringProperty(required = True)
  date = db.DateTimeProperty(required = True, auto_now_add=True)

class Page(webapp2.RequestHandler):
  def getBrowser(self):
    browser = self.request.get('browser')
    return re.sub('[^0-9a-zA-Z_]', '0', browser)

  def render(self, name):
    path = os.path.join(os.path.dirname(__file__), 'static/' + name)
    self.response.out.write(template.render(path, {
      'base_url' : 'send-to-computer.appspot.com',
      'browser_id' : self.getBrowser()
    }))

  def store(self):
    url = self.request.get('url')
    browser = self.getBrowser()
    if url:
      Item(browser = browser, url = url).put()

    # Remove a flag in memcache indicating empty datastore
    memcache.delete(key = browser)
    # logging.debug("memcache: event for %s" % browser)
    channel.send_message(browser, url)

  def date_rfc1123(self, t):
    return datetime.datetime.utcfromtimestamp(t).strftime("%a, %d %b %Y %H:%M:%S GMT")

  def cache(self, cachetime = 86400):
    now = time.time()
    self.response.headers["Date"] = self.date_rfc1123(now)
    self.response.headers["Expires"] = self.date_rfc1123(now + cachetime)
    self.response.headers["Cache-control"] = "public, max-age=%d" % cachetime

  def nocache(self):
    now = time.time()
    self.response.headers["Date"] = self.date_rfc1123(now)
    self.response.headers["Expires"] = "Mon, 01 Jan 1990 00:00:00 GMT"
    self.response.headers["Pragma"] = "no-cache"
    self.response.headers["Cache-control"] = "no-cache, must-revalidate"

class MainPage(Page):
  def __init__(self, a, b):
    super(MainPage, self).__init__(a, b);
    self.random = SystemRandom();

  def get(self):
    id = str(self.random.random())[2:]
    self.nocache()
    self.redirect("/me?browser=" + id)

class AndroidSubmit(Page):
  def post(self):
    self.store()
    self.response.headers['Content-type'] = 'text/plain'

class Send(Page):
  def get(self):
    self.cache(600)
    self.render('send.html')

class Count(Page):
  def get(self):
    self.nocache()
    items = db.GqlQuery(
        "SELECT __key__ FROM Item WHERE browser = :1", self.getBrowser())
    count = 0
    for item in items:
      count += 1
    self.response.out.write("%d" % count);

class Done(Page):
  def post(self):
    self.store()
    self.redirect("/send?browser=" + self.getBrowser())


class Poll(Page):
  def get(self):
    self.nocache()
    self.response.headers['Content-type'] = 'text/plain'
    browser = self.getBrowser()

    # Verify memcache flag indicating empty datastore
    if memcache.get(key = browser):
      # logging.debug("memcache: datastore is empty for %s" % browser)
      return

    # logging.debug("memcache: miss for %s" % browser)
    empty = True
    items = db.GqlQuery(
        "SELECT * FROM Item WHERE browser = :1", browser)
    for item in items:
      self.response.out.write(item.url);
      item.delete()
      empty = False
      break

    if empty:
      # Set a flag indicating empty datastore
      memcache.set(key = browser, value = True, time = 3600)
      # logging.debug("memcache: remember that datastore is empty for %s" % browser)

class Me(Page):

  def render(self, name):
    path = os.path.join(os.path.dirname(__file__), 'static/' + name)
    browser = self.getBrowser()
    self.response.out.write(template.render(path, {
      'base_url' : 'send-to-computer.appspot.com',
      'browser_id' : browser,
    }))

  def get(self):
    self.cache(600)
    self.render('sendtocomputer.html')

class Channel(Page):
  def makechannel(self):
    browser = self.getBrowser()
    channel_id = channel.create_channel(browser)
    self.response.out.write(channel_id)

  def get(self):
    self.nocache()
    self.makechannel()


class Associate(Page):
  def get(self):
    self.cache(600)
    uastring = self.request.headers.get('user_agent')
    if "Android" in uastring and "Linux" in uastring:
      self.render('associate_android.html')
    else:
      self.render('associate.html')

application = webapp2.WSGIApplication([
  ('/', MainPage),
  ('/me', Me),
  ('/count', Count),
  ('/poll', Poll),
  ('/send', Send),
  ('/done', Done),
  ('/submit', AndroidSubmit),
  ('/channel', Channel),
  ('/associate', Associate),
], debug=True)
