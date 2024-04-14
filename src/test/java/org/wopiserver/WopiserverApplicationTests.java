package org.wopiserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wopiserver.documents.DocumentService;

@SpringBootTest
class WopiserverApplicationTests {

	@Autowired DocumentService documentService;
	
	@Test
	void test() {
	}

}
