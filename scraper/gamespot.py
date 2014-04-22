from bs4 import BeautifulSoup
from pprint import pprint
from urlparse import urljoin
import requests
import re
import types
import os
import string

DOMAIN = 'http://www.gamespot.com'
START_URL = 'http://www.gamespot.com/reviews/?page='
DELSET = ''.join(chr(c) if chr(c).isalnum() else '_' for c in range(256))
DUMP_DIR = os.path.join(os.getcwd(),'gamespot')

def construct_urls():
	l = map(str, range(1,2))
	return l
STARTING_URLS = [START_URL + l for l in construct_urls()]

def spider_reviews_list(url):
	response = requests.get(url)
	soup = BeautifulSoup(response.content)
	reviews = soup.find_all('article', attrs={'class':'media'})

	for review in reviews:
		link = review.find('a').attrs['href']
		url = urljoin(DOMAIN, link)
		scrape_review_text(url)

def scrape_review_text(url):
	response = requests.get(url)
	soup = BeautifulSoup(response.content)

	title = soup.find('span', attrs={'itemprop':'itemreviewed'}).text
	title = title.replace(u'\xa0','').encode("utf8","ignore").strip().lower()
	# Replaces non-alphanumeric characters with underscores
	title = title.translate(DELSET)
	# Replaces consecutive underscores with a single one
	title = re.sub('_+', '_', title)
	print title

	review_section = soup.find('section', attrs={'itemprop':'description'})
	
	plist = []
	paragraphs = review_section.find_all('p', recursive=False)
	for p in paragraphs:
		plist.append(p.getText())

	filename = title + '.text'
	file = open(os.path.join(DUMP_DIR, filename), 'w')
	file.write('\n'.join(plist))
	file.close()

if __name__ == '__main__':
	if not os.path.exists(DUMP_DIR):
		os.mkdir(DUMP_DIR)

	#scrape_review_text('http://www.gamespot.com/reviews/nes-remix-2-review/1900-6415738/')

	for url in STARTING_URLS:
		spider_reviews_list(url)
	
