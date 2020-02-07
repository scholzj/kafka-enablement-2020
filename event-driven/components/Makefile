RELEASE_VERSION ?= latest

SUBDIRS=position-manager share-price-generator share-position-generator position-aggregator portfolio-aggregator pricer portfolio-value portfolio-viewer priced-portfolio-viewer price-viewer
DOCKER_TARGETS=docker_build docker_push docker_tag

all: $(SUBDIRS)
build: $(SUBDIRS)
clean: $(SUBDIRS)
$(DOCKER_TARGETS): $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: all $(SUBDIRS) $(DOCKER_TARGETS)
