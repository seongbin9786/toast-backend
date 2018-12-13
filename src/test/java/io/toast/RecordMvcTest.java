package io.toast;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(RecordController.class) // 이 Controller만 Bean으로 올림. 명시하지 않으면 모두 올림.
public class RecordMvcTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RecordController rc;
	
	@MockBean
	private RecordRepository repo;
	
	@MockBean
	private FileUploadManager manager;
	
	private ObjectMapper mapper = new ObjectMapper();

	private static final String URL = "/records";
		
	@After
	public void setMockedManager() {
		rc.setFileUploadManager(manager); // mock된 manager로 다시 변경
	}
	
	@Test
	public void 녹음_파일을_업로드할_주소는_POST_records_여야_한다() throws Exception {
		// given=URL

		MockHttpServletResponse response = mockMvc.perform(multipart(URL)).andReturn().getResponse();
		
		// when
		int statusCode = response.getStatus();
		String message = response.getErrorMessage();
		
		// then
		// 405[Method Not Allowed - 엔드포인트에서 해당 Method를 지원하지 않는 경우] 및
		// 404[Not Found - 엔드포인트가 아예 없는 경우]를 반환하면 안 됨
		assertNotEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), statusCode);
		assertNotEquals(HttpStatus.NOT_FOUND.value(), statusCode);
		assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode);
		assertEquals(NoFileUploadedException.MESSAGE, message);
	}
	
	@Test
	public void 개별_조회시_FileUploadManager_getFileByRecord를_호출한다() throws Exception {
		// given - repo를 mock함
		when(repo.findById(Mockito.anyLong())).thenReturn(Optional.of(new Record(1L)));

		// when - 개별 조회 시
		mockMvc.perform(get(URL + "/" + 1)).andExpect(status().isOk());
		
		// then - manager#getFileByRecord 1회 호출
		verify(manager, times(1)).getFileByRecord(Mockito.any(Record.class));
	}
	
	@Test
	public void 예외테스트_없는_ID로_조회시_NOT_FOUND를_반환해야_한다() throws Exception {
		// given - repo를 mock함
		when(repo.getOne(Mockito.anyLong())).thenReturn(null);

		// when - 없는 ID로 조회 시
		Long 없는_ID = 999999L;
		String errMsg = mockMvc.perform(get(URL + "/" + 없는_ID))
		
		// then - 1 - 404 NOT FOUND
		.andExpect(status().isNotFound())
		.andReturn().getResponse().getErrorMessage();

		// then - 2 - 적절한 ErrorMsg 반환
		assertThat(errMsg, is(NoRecordException.NO_RECORD_MSG));
		
	}
	
	@Test
	public void 전체_조회시_repo의_findAll을_호출하고_그_반환값을_반환한다() throws Exception {
		// given - 1 - repo를 mock
		List<Record> records = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			records.add(new Record(Long.valueOf(i)));
		
		when(repo.findAll()).thenReturn(records);
		
		// when
		String recordsJson = mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		// then - 1 - repo의 findAll을 호출해야 함
		verify(repo, times(1)).findAll();
		
		// then - 2 - repo가 반환한 값을 그대로 반환하는지 확인
		List<Record> recordsFromJson = Arrays.asList(mapper.readValue(recordsJson, Record[].class));
		assertTrue(CollectionUtils.isEqualCollection(records, recordsFromJson));
	}
	
}
