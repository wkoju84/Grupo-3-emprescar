package br.com.dh.emprescar.service;

import br.com.dh.emprescar.dto.ImageDto;
import br.com.dh.emprescar.dto.ProductDto;
import br.com.dh.emprescar.model.*;
import br.com.dh.emprescar.repository.CategoryRepository;
import br.com.dh.emprescar.repository.CityRepository;
import br.com.dh.emprescar.repository.FeatureRepository;
import br.com.dh.emprescar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.dh.emprescar.service.exceptions.EntityNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public List<ProductDto> searchAll() {
        List<Product> list = productRepository.findAll();
        return list.stream().map(x -> new ProductDto(x)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchAllByCategoryId(Integer categoryId) {
        List<Product> list = productRepository.findAllByCategoryId(categoryId);
        return list.stream().map(x -> new ProductDto(x)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchAllByCityId(Integer cityId) {
        List<Product> list = productRepository.findAllByCityId(cityId);
        return list.stream().map(x -> new ProductDto(x)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto searchById(Integer id) {
        Optional<Product> object = productRepository.findById(id);
        Product entity = object.orElseThrow(() -> new EntityNotFoundException("Entity or register " + id + " not found on database!" ));
        return new ProductDto(entity);
    }

    @Transactional
    public ProductDto insert(ProductDto dto) {
        Product entity = copyDtoToEntity(dto, new Product());
        Optional<City> city = cityRepository.findById(dto.getCity().getId());
        Optional<Category> category = categoryRepository.findById(dto.getCategory().getId());
        List<Feature> features = featureRepository.findByIdIn(dto.getFeatures().stream().map(x -> x.getId()).toList());
        if (city.isPresent()) {
            entity.setCity(city.get());
        }
        if (category.isPresent()) {
            entity.setCategory(category.get());
        }
        entity.setFeatures(new HashSet<Feature>(features));

        entity = productRepository.saveAndFlush(entity);

        return new ProductDto(entity);
    }

    @Transactional
    public ProductDto update(ProductDto dto) {
        try {
            Product entity = productRepository.getReferenceById(dto.getId());
            City city = cityRepository.getReferenceById(dto.getCity().getId());
            Category category = categoryRepository.getReferenceById(dto.getCategory().getId());
            List<Feature> features = featureRepository.findByIdIn(dto.getFeatures().stream().map(x -> x.getId()).toList());

            entity.setCity(city);
            entity.setCategory(category);
            entity.setFeatures(new HashSet<Feature>(features));

            entity = productRepository.save(entity);
            return new ProductDto(entity);
        }
        catch (EntityNotFoundException entityNotFoundException) {
            throw new EntityNotFoundException(
                    "\"Entity or register \" + id + \" Not found on database!\""
            );
        }
    }

    @Transactional
    public void delete(Integer id) {
        try {
            productRepository.deleteById(id);
        }
        catch (EntityNotFoundException entityNotFoundException) {
            throw new EntityNotFoundException(
                    "Error on Deleting: Register " + id + " not found on your database"
            );
        }
    }

    private Product copyDtoToEntity(ProductDto dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setImages(dto.getImages().stream().map(x -> copyDtoToEntity(x, entity)).collect(Collectors.toSet()));
        return entity;
    }

    private Image copyDtoToEntity(ImageDto dto, Product entity) {
        Image image =  new Image();
        image.setProduct(entity);
        image.setTitle(dto.getTitle());
        image.setUrl(dto.getUrl());
        return image;
    }

    public List<ProductDto> searchAllByDateRange(Date pickupDate, Date dropoffDate) {
        List<Product> list = productRepository.findAllByDateRange(pickupDate, dropoffDate);
        return list.stream().map(x -> new ProductDto(x)).collect(Collectors.toList());
    }

    public List<ProductDto> searchAllByCityIdAndDateRange(Integer cityId, Date pickupDate, Date dropoffDate) {
        List<Product> list = productRepository.findAllByCityIdAndDateRange(cityId, pickupDate, dropoffDate);
        return list.stream().map(x -> new ProductDto(x)).collect(Collectors.toList());
    }
}
