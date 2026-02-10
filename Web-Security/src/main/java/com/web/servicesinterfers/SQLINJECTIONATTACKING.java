package com.web.servicesinterfers;

import java.util.List;

import com.web.webDTO.SQLinjectionDTO;

public interface SQLINJECTIONATTACKING {
	
	List<String> runTest(SQLinjectionDTO request);
}
