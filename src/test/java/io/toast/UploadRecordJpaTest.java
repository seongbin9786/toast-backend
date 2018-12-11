package io.toast;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UploadRecordJpaTest {

	@Autowired
	private RecordRepository repo;
	
	@Test
	public void Record를_매번_저장할_때_마다_ID가_새로_생성되어야_한다() throws Exception {
		Record a = new Record();
		Record b = new Record();
		
		repo.save(a);
		repo.save(b);
		
		assertNotEquals(a.getId(), b.getId());
	}
	
	@Test
	public void Record는_무엇을_어떻게_테스트짜기_참_힘들죠() {
		
	}
}
