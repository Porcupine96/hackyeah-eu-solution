class Document:
    def __init__(self, name, page, content):
        self.name = name
        self.page = page
        self.content = content
        self._id = "{}-{}".format(name, page)

    def __hash__(self):
        return hash(self._id)

    def __repr__(self):
        return self._id
