import requests
from bs4 import BeautifulSoup
from pprint import pprint
from urlparse import urljoin
import re
import types
import os
import string

DOMAIN = 'http://www.destructoid.com'
START_URL = 'http://www.destructoid.com/products_index.phtml?display=short&filt=reviews&alpha='
DELSET = ''.join(chr(c) if chr(c).isalnum() else '_' for c in range(256))
DUMP_DIR = os.path.join(os.getcwd(),'destructoid')

def construct_urls():
	l = list(string.ascii_lowercase)
	l = ['other'] + l
	return l
STARTING_URLS = [START_URL + a for a in construct_urls()]



def spider_reviews_list(url):
	response = requests.get(url)

	soup = BeautifulSoup(response.content)
	review_table = soup.find('table', attrs={'class':'products_table'})
	reviews = review_table.find_all('a', text = re.compile('Review'))

	for review in reviews:
		link = review.attrs['href']
		url = urljoin(DOMAIN, link)
		scrape_review_text(url)

	next_page = soup.find('a', text = re.compile('NEXT'))
	if next_page is not None:
		page_url = urljoin(DOMAIN, next_page.attrs['href'])
		spider_reviews_list(page_url)



def scrape_review_text(url):
	response = requests.get(url)
	soup = BeautifulSoup(response.content)
	try:
		title = soup.find('a', attrs={'class':'product_name_subnav'}).text
		title = title.replace(u'\xa0','').encode("utf8","ignore").strip().lower()
		
		# Replaces non-alphanumeric characters with underscores
		title = title.translate(DELSET)
		# Replaces consecutive underscores with a single one
		title = re.sub('_+', '_', title)

		print title
		plist = []
		paragraphs = soup.find('div', attrs={'class':'large_post_container'}).find_all('p')
		for p in paragraphs:
			ptext = br_replacer(p)
			encoded_text = re.sub('</br>','\n',ptext).encode("utf8","ignore")
			plist.append(encoded_text)
		filename = title+'.txt'
		file = open(os.path.join(DUMP_DIR, filename), 'w')
		file.write('\n'.join(plist))
		file.close()
	except:
		print 'Exception for {url}'.format(url=url)


def br_replacer(element):
	t = []
	for elem in element.recursiveChildGenerator():
		if isinstance(elem, types.StringTypes):
			t.append(elem.strip())
		elif elem.name == 'br':
			t.append('\n')

	return ' '.join(t)

if __name__ == '__main__':
	if not os.path.exists(DUMP_DIR):
		os.mkdir(DUMP_DIR)

	for url in STARTING_URLS:
		spider_reviews_list(url)
	#scrape_review_text('http://www.destructoid.com/review-metal-gear-solid-v-ground-zeroes-271878.phtml')
	#scrape_review_text('http://www.destructoid.com/review-nine-hours-nine-persons-nine-doors-187838.phtml')