Each news has several files:
XX.t is the news title.
XX.n is the news body.
XX-i?-HD.jpg are high resolution (>=5kb) images for the news item.
XX-i?-LD.jpg are low resolution (<5kb) images for the news item.

To create the low resolution from the high resolution images, use the build-ld.sh script:
$ build-ld

To remove all temporary files:
$ clean

To generate the SQL code to load the news:
$ gen-sql

To copy all images to the znn directory:
$ rebuild-image-dir

The news items were the first 10 items in slashdot news on Jun 6th, 2012.
