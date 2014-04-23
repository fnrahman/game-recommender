from bs4 import BeautifulSoup
from pprint import pprint
from urlparse import urljoin
import requests
import re
import types
import os
import string
import traceback

DOMAIN = 'http://www.gamespot.com'
START_URL = 'http://www.gamespot.com/reviews/?page='
DELSET = ''.join(chr(c) if chr(c).isalnum() else '_' for c in range(256))
DUMP_DIR = os.path.join(os.getcwd(),'gamespot')

def construct_urls():
	# Pages range from 1 <= to <= 644
	# remember that range(x,y) goes from x <= n < y !!!
	# 1 - 6 done
	# 6 - 11 done
	# 11- 26 done
	# 26 - 51 done
	# 51 - 76 done
	l = map(str, range(76,101))
	return l
STARTING_URLS = [START_URL + l for l in construct_urls()]

def spider_reviews_list(url):
	print "~~~~ ON PAGE " + url + " ~~~~"
	response = requests.get(url)
	soup = BeautifulSoup(response.content)
	review_list = soup.find('section', attrs={'class':'editorial'})
	reviews = review_list.find_all('article', attrs={'class':'media'})

	for review in reviews:
		link = review.find('a').attrs['href']
		url = urljoin(DOMAIN, link)
		scrape_review_text(url)

def scrape_review_text(url):
	response = requests.get(url)
	soup = BeautifulSoup(response.content)
	try:
		title = 'TEMP'

		review_end = False
		# Try obtaining the tag that holds the game name in 4 cascading ways
		game_name = soup.find('span', attrs={'itemprop':'itemreviewed'})
		if game_name is None:
			print
			game_name = soup.find('h1', attrs={'class':'kubrick-info__title'})
			if game_name is None:
				game_name = soup.find('h1', attrs={'class':'entry-title'})
				review_end = True
				if game_name is None:
					game_name = soup.find('dt', attrs={'class':'pod-objectStats__title'}).find('h3')

		title = game_name.text
		title = title.replace('Review','') 
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
			plist.append(p.getText().encode("utf8","ignore"))
		filename = title + '.text'
		file = open(os.path.join(DUMP_DIR, filename), 'w')
		file.write('\n'.join(plist))
		file.close()
	except:
		print 'Exception for {url}'.format(url=url)
		traceback.print_exc()

if __name__ == '__main__':
	if not os.path.exists(DUMP_DIR):
		os.mkdir(DUMP_DIR)

	#scrape_review_text('http://www.gamespot.com/reviews/dishonored-the-brigmore-witches-review/1900-6413130/')

	for url in STARTING_URLS:
		spider_reviews_list(url)
	
