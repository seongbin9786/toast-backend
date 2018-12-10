package io.toast;

import static org.junit.Assert.assertNotNull;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ToastBackendApplicationTests {

	@Autowired
	private DataSource dataSource;
	
	@Test
	public void JPA를_Depdendency로_놓는_경우_dataSource_가_있어야_한다() {
		assertNotNull(dataSource);
	}

}
