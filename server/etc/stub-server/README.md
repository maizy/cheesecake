# Stub server for testing

[Sample config](../../docs/sample-config.conf) used that stub.

### Requirements

* python 2.7
* [pip](http://pip.readthedocs.org/en/latest/installing.html) (python package tools)
* [zaglushka.py](https://github.com/maizy/zaglushka) (`pip install git+https://github.com/maizy/zaglushka.git`)

### Run

```
cd cheesecake/
zaglushka.py --ports=52030 --config=server/etc/stub-server/zaglushka.config
```
