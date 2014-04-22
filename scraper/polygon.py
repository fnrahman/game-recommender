from bs4 import BeautifulSoup
from pprint import pprint
from urlparse import urljoin
import requests
import re
import types
import os
import string

DOMAIN = 'http://www.polygon.com'
START_URL = 'http://www.polygon.com/games/reviewed/'
DELSET = ''.join(chr(c) if chr(c).isalnum() else '_' for c in range(256))
DUMP_DIR = os.path.join(os.getcwd(),'polygon')

def construct_urls():
	# Pages range from 1 to 15
	l = map(str, range(1,15))
	return l
STARTING_URLS = [START_URL + l for l in construct_urls()]

def spider_reviews_list(url):
	response = requests.get(url)
	soup = BeautifulSoup(response.content)
	reviews = soup.find('ul', attrs={'class', 'game_list'}).find_all('li')

	for review in reviews:
		link = review.find('a', attrs={'class', 'review_link'}).attrs['href']
		url = urljoin(DOMAIN, link)
		scrape_review_text(url)

def scrape_review_text(url):
	response = requests.get(url):