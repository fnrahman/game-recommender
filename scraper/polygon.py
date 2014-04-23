from bs4 import BeautifulSoup
from pprint import pprint
from urlparse import urljoin
import requests
import re
import types
import os
import string
import traceback

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
	reviews = soup.find('ul', attrs={'class', 'game_list'}).find_all('div', attrs={'class':'body'})

	for review in reviews:
		links = review.find_all('a')
		for link in links:
			if link.has_attr('class'):
				review_link = link.attrs['href']
			else:
				title = link.text
				title = title.replace(u'\xa0','').encode("utf8","ignore").strip().lower()
				# Replaces non-alphanumeric characters with underscores
		title = title.translate(DELSET)
		# Replaces consecutive underscores with a single one
		title = re.sub('_+', '_', title)

		print title
		scrape_review_text(urljoin(DOMAIN, review_link), title)

def scrape_review_text(url,title):
	response = requests.get(url)
	soup = BeautifulSoup(response.content)
	try:
		review_section = soup.find('section', attrs={'class':'body'})
		paragraphs = review_section.find_all('p')
		
		plist = []
		for p in paragraphs:
			plist.append(p.getText().encode('utf8','ignore'))
		filename = title + '.txt'
		file = open(os.path.join(DUMP_DIR, filename), 'w')
		file.write('\n'.join(plist))
		file.close()
	except:
		print 'Exception for {url}'.format(url=url)
		traceback.print_exc()


if __name__ == '__main__':
	if not os.path.exists(DUMP_DIR):
		os.mkdir(DUMP_DIR)

	for url in STARTING_URLS:
		spider_reviews_list(url)