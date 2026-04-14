package com.emms.backend.service;

import com.emms.backend.dto.reading.ReadingDTO;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.Reading;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.MeterRepository;
import com.emms.backend.repository.ReadingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final MeterRepository meterRepository;

    public ReadingService(ReadingRepository readingRepository,
                          MeterRepository meterRepository) {
        this.readingRepository = readingRepository;
        this.meterRepository = meterRepository;
    }

    public Reading create(ReadingDTO dto) {
        validateDto(dto, true);

        Meter meter = meterRepository.findById(dto.getMeterId())
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy meter với id: " + dto.getMeterId(),
                        HttpStatus.NOT_FOUND
                ));

        Reading reading = new Reading();
        reading.setValue(dto.getValue());
        reading.setMeter(meter);
        reading.setRecordedAt(dto.getRecordedAt() != null ? dto.getRecordedAt() : LocalDateTime.now());

        return readingRepository.save(reading);
    }

    public Reading update(Long readingId, ReadingDTO dto) {
        if (readingId == null) {
            throw new CustomException("readingId không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("ReadingDTO không được để trống", HttpStatus.BAD_REQUEST);
        }

        Reading savedReading = readingRepository.findById(readingId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy reading với id: " + readingId,
                        HttpStatus.NOT_FOUND
                ));

        if (dto.getValue() != null) {
            if (dto.getValue() < 0) {
                throw new CustomException("Giá trị đo không được âm", HttpStatus.BAD_REQUEST);
            }
            savedReading.setValue(dto.getValue());
        }

        if (dto.getMeterId() != null) {
            Meter meter = meterRepository.findById(dto.getMeterId())
                    .orElseThrow(() -> new CustomException(
                            "Không tìm thấy meter với id: " + dto.getMeterId(),
                            HttpStatus.NOT_FOUND
                    ));
            savedReading.setMeter(meter);
        }

        if (dto.getRecordedAt() != null) {
            savedReading.setRecordedAt(dto.getRecordedAt());
        }

        return readingRepository.save(savedReading);
    }

    @Transactional(readOnly = true)
    public Collection<Reading> getAll() {
        return readingRepository.findAll();
    }

    public void delete(Long readingId) {
        if (readingId == null) {
            throw new CustomException("readingId không được để trống", HttpStatus.BAD_REQUEST);
        }

        Reading reading = readingRepository.findById(readingId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy reading với id: " + readingId,
                        HttpStatus.NOT_FOUND
                ));

        readingRepository.delete(reading);
    }

    @Transactional(readOnly = true)
    public Reading getById(Long readingId) {
        if (readingId == null) {
            throw new CustomException("readingId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return readingRepository.findById(readingId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy reading với id: " + readingId,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public Collection<Reading> findByMeter(Long meterId) {
        if (meterId == null) {
            throw new CustomException("meterId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return readingRepository.findByMeter_MeterIdOrderByRecordedAtDesc(meterId);
    }

    @Transactional(readOnly = true)
    public Optional<Reading> findLatestByMeter(Long meterId) {
        if (meterId == null) {
            throw new CustomException("meterId không được để trống", HttpStatus.BAD_REQUEST);
        }
        return readingRepository.findTopByMeter_MeterIdOrderByRecordedAtDesc(meterId);
    }

    private void validateDto(ReadingDTO dto, boolean requireMeterId) {
        if (dto == null) {
            throw new CustomException("ReadingDTO không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getValue() == null) {
            throw new CustomException("Giá trị đo không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getValue() < 0) {
            throw new CustomException("Giá trị đo không được âm", HttpStatus.BAD_REQUEST);
        }

        if (requireMeterId && dto.getMeterId() == null) {
            throw new CustomException("meterId không được để trống", HttpStatus.BAD_REQUEST);
        }
    }
}